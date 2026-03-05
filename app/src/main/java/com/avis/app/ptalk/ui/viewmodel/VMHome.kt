package com.avis.app.ptalk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avis.app.ptalk.core.network.DeviceDto
import com.avis.app.ptalk.core.network.IoTPlatformApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VMHome @Inject constructor(
    private val api: IoTPlatformApi
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val devices: List<DeviceDto> = emptyList()
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

                android.util.Log.d("API", "Request GET /api/v1/devices")

                val devices = api.getMyDevices()

                android.util.Log.d("API", "Devices response size = ${devices.size}")
                android.util.Log.d("API", "Devices response = $devices")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    devices = devices
                )

            } catch (e: Exception) {

                if (e is retrofit2.HttpException) {
                    val code = e.code()
                    val errorBody = e.response()?.errorBody()?.string()

                    android.util.Log.e("API", "HTTP ERROR CODE = $code")
                    android.util.Log.e("API", "SERVER ERROR BODY = $errorBody")
                }

                android.util.Log.e("API", "Load devices error", e)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Lỗi tải danh sách thiết bị"
                )
            }
        }
    }
}
