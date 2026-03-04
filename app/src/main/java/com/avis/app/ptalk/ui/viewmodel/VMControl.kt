package com.avis.app.ptalk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avis.app.ptalk.core.websocket.DeviceStatusResponse
import com.avis.app.ptalk.domain.service.DeviceControlService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VMControl @Inject constructor(
    private val controlService: DeviceControlService
) : ViewModel() {

    val deviceStatus: StateFlow<DeviceStatusResponse?> = controlService.deviceStatus
    val isLoading: StateFlow<Boolean> = controlService.isLoading
    val connectionState: StateFlow<Boolean> = controlService.connectionState
    val lastError: StateFlow<String?> = controlService.lastError

    private val _deviceId = MutableStateFlow<String?>(null)
    val deviceId = _deviceId.asStateFlow()

    fun initConnection(macAddress: String) {
        _deviceId.value = macAddress
        controlService.connect(macAddress)
        // Request initial status when connected
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            if (connectionState.value) {
                controlService.refreshStatus()
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
