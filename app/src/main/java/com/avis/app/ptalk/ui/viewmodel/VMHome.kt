package com.avis.app.ptalk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avis.app.ptalk.core.network.DeviceResponse
import com.avis.app.ptalk.core.network.IoTPlatformApi
import com.avis.app.ptalk.domain.data.local.repo.AuthRepository
import com.avis.app.ptalk.domain.data.local.repo.DeviceRepository
import com.avis.app.ptalk.domain.model.Device
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
    private val deviceRepository: DeviceRepository
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

    fun signOut() {
        authRepository.logout()
    }
}
