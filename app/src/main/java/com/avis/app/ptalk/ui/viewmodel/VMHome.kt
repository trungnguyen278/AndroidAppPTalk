package com.avis.app.ptalk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avis.app.ptalk.core.network.DeviceResponse
import com.avis.app.ptalk.core.network.IoTPlatformApi
import com.avis.app.ptalk.domain.data.local.repo.AuthRepository
import com.avis.app.ptalk.domain.data.local.repo.DeviceRepository
import com.avis.app.ptalk.domain.model.Device
import com.avis.app.ptalk.domain.service.DeviceControlService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VMHome @Inject constructor(
    private val api: IoTPlatformApi,
    private val authRepository: AuthRepository,
    private val deviceRepository: DeviceRepository,
    private val controlService: DeviceControlService
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val devices: List<Device> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadDevices()
    }

    fun loadDevices() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // Collect devices from local Room database
                deviceRepository.devices().collect { devices ->
                    android.util.Log.d("VMHome", "Local devices count = ${devices.size}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        devices = devices
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("VMHome", "Load devices error", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Lỗi tải danh sách thiết bị"
                )
            }
        }
    }

    /**
     * Delete device from local DB and send request_ble_config to put it back in BLE mode
     */
    fun deleteDevice(device: Device) {
        viewModelScope.launch {
            try {
                // Send request_ble_config to device via MQTT before removing
                val deviceId = device.deviceId ?: device.macAddress.replace(":", "").lowercase()
                controlService.connect(deviceId)
                // Give time for MQTT to connect then send BLE config command
                kotlinx.coroutines.delay(2000)
                controlService.requestBleConfig()
                kotlinx.coroutines.delay(500)
                controlService.disconnect()

                // Delete from local DB
                deviceRepository.delete(device.macAddress)
                android.util.Log.d("VMHome", "Device deleted: ${device.macAddress}")
            } catch (e: Exception) {
                android.util.Log.e("VMHome", "Delete device error", e)
            }
        }
    }

    fun signOut() {
        authRepository.logout()
    }

    fun getUsername(): String? = authRepository.getUsername()
    fun getEmail(): String? = authRepository.getEmail()
    fun getPhone(): String? = authRepository.getPhone()
    fun getUserId(): String? = authRepository.getUserId()
}
