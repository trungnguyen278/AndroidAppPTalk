package com.avis.app.ptalk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avis.app.ptalk.core.ble.BleClient
import com.avis.app.ptalk.core.ble.ScannedDevice
import com.avis.app.ptalk.domain.control.ControlGateway
import com.avis.app.ptalk.domain.control.WifiNetwork
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.thingai.base.log.ILog
import javax.inject.Inject

/**
 * ViewModel for simplified config-only app
 * No database storage - just BLE config and forget
 */
@HiltViewModel
class VMConfigDevice @Inject constructor(
    private val ble: BleClient,
    private val controlGateway: ControlGateway
) : ViewModel() {
    private val TAG = "VMConfigDevice"

    data class UiState(
        val scanning: Boolean = false,
        val devices: List<ScannedDevice> = emptyList(),
        val error: String? = null,
        val wifiNetworks: List<WifiNetwork> = emptyList(),
        val loadingWifiList: Boolean = false,
        val deviceId: String = ""
    )

    private val _ui = MutableStateFlow(UiState())
    val ui = _ui.asStateFlow()

    private var scanJob: Job? = null
    private var deviceAddress: String? = null

    fun startScan() {
        scanJob?.cancel()
        _ui.value = _ui.value.copy(scanning = true, error = null, devices = emptyList())

        scanJob = ble.scanForConfigDevices()
            .onEach { list -> _ui.value = _ui.value.copy(devices = list) }
            .catch { e -> _ui.value = _ui.value.copy(error = e.message, scanning = false) }
            .let { flow ->
                viewModelScope.launch { flow.collect { } }
            }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        _ui.value = _ui.value.copy(scanning = false)
    }

    fun connectDevice(deviceAddress: String, onConnected: () -> Unit) {
        this.deviceAddress = deviceAddress
        viewModelScope.launch {
            try {
                // Set loading state
                _ui.value = _ui.value.copy(loadingWifiList = true)
                
                // Connect to device
                controlGateway.connect(deviceAddress)
                
                // Wait for connection to be established (max 5 seconds)
                var connected = false
                var attempts = 0
                while (!connected && attempts < 50) {
                    kotlinx.coroutines.delay(100)
                    attempts++
                    // Check if session is connected by trying to read
                    try {
                        controlGateway.readDeviceId()
                        connected = true
                    } catch (e: Exception) {
                        // Not yet connected, keep waiting
                    }
                }
                
                if (!connected) {
                    throw Exception("Connection timeout")
                }
                
                // Now load device info
                loadDeviceInfo()
                
                onConnected()
            } catch (e: Exception) {
                ILog.e(TAG, "connectDevice failed", e.message)
                _ui.value = _ui.value.copy(loadingWifiList = false, error = e.message)
            }
        }
    }

    private suspend fun loadDeviceInfo() {
        try {
            _ui.value = _ui.value.copy(loadingWifiList = true)

            val deviceIdResult = try {
                controlGateway.readDeviceId()
            } catch (e: Exception) {
                ILog.e(TAG, "readDeviceId failed", e.message)
                ""
            }

            val wifiList = try {
                controlGateway.readWifiList()
            } catch (e: Exception) {
                ILog.e(TAG, "readWifiList failed", e.message)
                emptyList()
            }

            _ui.value = _ui.value.copy(
                wifiNetworks = wifiList,
                deviceId = deviceIdResult,
                loadingWifiList = false
            )
            ILog.d(TAG, "loadDeviceInfo", "deviceId=$deviceIdResult, networks=${wifiList.size}")
        } catch (e: Exception) {
            _ui.value = _ui.value.copy(loadingWifiList = false, error = e.message)
            ILog.e(TAG, "loadDeviceInfo", e.message)
        }
    }

    fun refreshWifiList() {
        viewModelScope.launch {
            loadDeviceInfo()
        }
    }

    fun disconnectDevice() {
        viewModelScope.launch {
            controlGateway.disconnect()
            deviceAddress = null
            // Clear device info so it will be reloaded on next connect
            _ui.value = _ui.value.copy(
                wifiNetworks = emptyList(),
                deviceId = "",
                loadingWifiList = false
            )
        }
    }

    /**
     * Configure device without saving to database
     * Just write config via BLE and disconnect
     */
    fun configDevice(
        ssid: String,
        password: String,
        volume: Float,
        brightness: Float,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                ILog.d(TAG, "configDevice", "Starting config with ssid=$ssid")
                
                controlGateway.writeWifiSsid(ssid)
                controlGateway.writeWifiPass(password)
                controlGateway.writeVolume((volume * 100).toInt())
                controlGateway.writeBrightness((brightness * 100).toInt())
                controlGateway.saveConfig()

                ILog.d(TAG, "configDevice", "Config saved successfully")
                onSuccess()
            } catch (e: Exception) {
                ILog.e(TAG, "configDevice", e.message)
                onError(e.message ?: "Lỗi không xác định")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
        viewModelScope.launch {
            controlGateway.disconnect()
        }
    }
}
