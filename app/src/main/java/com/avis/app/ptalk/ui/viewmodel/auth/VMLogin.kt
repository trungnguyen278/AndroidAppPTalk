package com.avis.app.ptalk.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avis.app.ptalk.domain.data.local.repo.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VMLogin @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val success: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = UiState(error = "Vui lòng nhập đầy đủ thông tin")
            return
        }

        _uiState.value = UiState(isLoading = true)

        viewModelScope.launch {
            val result = authRepo.login(email, pass)
            
            if (result.isSuccess) {
                _uiState.value = UiState(success = true)
            } else {
                val e = result.exceptionOrNull()
                val msg = if (e?.message?.contains("401") == true || e?.message?.contains("404") == true) {
                    "Email hoặc mật khẩu không đúng"
                } else {
                    e?.message ?: "Đăng nhập thất bại"
                }
                _uiState.value = UiState(error = msg)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
