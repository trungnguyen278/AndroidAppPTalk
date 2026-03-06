package com.avis.app.ptalk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avis.app.ptalk.core.websocket.DeviceStatusResponse
import com.avis.app.ptalk.domain.data.local.repo.DeviceRepository
import com.avis.app.ptalk.domain.service.DeviceControlService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VMControl @Inject constructor(
    private val controlService: DeviceControlService,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    val deviceStatus: StateFlow<DeviceStatusResponse?> = controlService.deviceStatus
    val isLoading: StateFlow<Boolean> = controlService.isLoading
    val connectionState: StateFlow<Boolean> = controlService.connectionState
    val lastError: StateFlow<String?> = controlService.lastError

    private val _deviceId = MutableStateFlow<String?>(null)
    val deviceId = _deviceId.asStateFlow()

    // Stores the locally saved device name from DB
    private val _localDeviceName = MutableStateFlow<String?>(null)

    fun initConnection(macAddress: String) {
        _deviceId.value = macAddress

        // Load saved device info from DB and connect
        viewModelScope.launch {
            val device = deviceRepository.get(macAddress)
            _localDeviceName.value = device?.name
            
            val mqttId = device?.deviceId ?: macAddress.replace(":", "").lowercase()
            android.util.Log.d("VMControl", "initConnection: mac=$macAddress, mqttId=$mqttId")
            controlService.connect(mqttId)
        }

        // When status arrives, check if device name needs syncing
        viewModelScope.launch {
            deviceStatus.collect { status ->
                if (status != null) {
                    val dbName = _localDeviceName.value
                    if (dbName != null && status.deviceName != null && dbName != status.deviceName) {
                        android.util.Log.d("VMControl", "Name mismatch: DB='$dbName' vs Device='${status.deviceName}'. Syncing to device...")
                        controlService.setDeviceName(dbName)
                    }
                }
            }
        }
    }

    fun setVolume(volume: Int) {
        controlService.setVolume(volume)
    }

    fun setBrightness(brightness: Int) {
        controlService.setBrightness(brightness)
    }

    fun setDeviceName(name: String) {
        val mac = _deviceId.value ?: return

        // Always save to local DB
        viewModelScope.launch {
            val device = deviceRepository.get(mac)
            if (device != null) {
                device.name = name
                deviceRepository.upsert(device)
                android.util.Log.d("VMControl", "Device name saved to DB: $name")
            }
            _localDeviceName.value = name
        }

        // Send MQTT command if connected
        controlService.setDeviceName(name)
    }

    fun rebootDevice() {
        controlService.rebootDevice()
    }

    fun resetWifi() {
        controlService.requestBleConfig()
    }

    fun clearError() {
        controlService.clearError()
    }

    override fun onCleared() {
        super.onCleared()
        controlService.disconnect()
    }
}
