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
class VMSignup @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val success: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun signup(
                    authUsername: String,
                    email: String,
                    pass: String,
                    passConfirm: String,
                    username: String,
                    phone: String
                ) {
        if (authUsername.isBlank() || email.isBlank() || pass.isBlank() || passConfirm.isBlank() || username.isBlank()) {
            _uiState.value = UiState(error = "Vui lòng nhập các trường bắt buộc")
            return
        }

        if (pass != passConfirm) {
            _uiState.value = UiState(error = "Mật khẩu không khớp")
            return
        }

        if (pass.length < 6) {
            _uiState.value = UiState(error = "Mật khẩu phải có ít nhất 6 ký tự")
            return
        }

        android.util.Log.d("API", "Signup request authUsername=$authUsername, email=$email, username=$username")
        _uiState.value = UiState(isLoading = true)

        viewModelScope.launch {
            val result = authRepo.signup(authUsername, email, pass, username, phone.ifBlank { null })
            
            if (result.isSuccess) {
                _uiState.value = UiState(success = true)
            } else {
                val e = result.exceptionOrNull()
                val msg = if (e?.message?.contains("409") == true || e?.message?.contains("400") == true) {
                    "Email hoặc tên người dùng đã tồn tại"
                } else {
                    e?.message ?: "Đăng ký thất bại"
                }
                _uiState.value = UiState(error = msg)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
