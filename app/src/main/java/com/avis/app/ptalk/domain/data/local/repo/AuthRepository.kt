package com.avis.app.ptalk.domain.data.local.repo

import com.avis.app.ptalk.core.network.IoTPlatformApi
import com.avis.app.ptalk.core.network.LoginRequest
import com.avis.app.ptalk.core.network.SignupRequest
import com.avis.app.ptalk.core.network.TokenManager
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: IoTPlatformApi,
    private val tokenManager: TokenManager
) {
    val isLoggedIn: StateFlow<Boolean> = tokenManager.isLoggedIn

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = api.login(LoginRequest(email, password))
            tokenManager.saveToken(response.access_token, response.user.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signup(email: String, password: String, username: String, phone: String?): Result<Unit> {
        return try {
            val response = api.signup(SignupRequest(email, password, username, phone))
            // After successful signup, auto login
            login(email, password)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager.clearToken()
    }
}