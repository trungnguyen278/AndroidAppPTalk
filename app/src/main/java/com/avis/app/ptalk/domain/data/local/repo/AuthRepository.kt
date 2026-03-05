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

   suspend fun login(username: String, password: String): Result<Unit> {
        return try {

            android.util.Log.d("API", "Login request username=$username password=$password")

            val response = api.login(LoginRequest(username, password))

            android.util.Log.d("API", "Login success token=${response.access_token}")
            android.util.Log.d("API", "Login user=${response.user}")

            val userId = response.user?.id ?: ""

            android.util.Log.d("API", "UserID=$userId")

            tokenManager.saveToken(response.access_token, userId)

            Result.success(Unit)

        } catch (e: Exception) {

            if (e is retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                android.util.Log.e("API", "Server error body = $errorBody")
            }

            android.util.Log.e("API", "Login error", e)
            Result.failure(e)
        }
    }

    suspend fun signup(
            authUsername: String,
            email: String,
            password: String,
            username: String,
            phone: String?
        ): Result<Unit> {
        return try {

            val request = SignupRequest(
                auth_username = authUsername,
                username = username,
                password = password,
                email = email,
                phone_number = phone
            )

            val response = api.signup(request)

            login(username, password)

        } catch (e: Exception) {

            if (e is retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                android.util.Log.e("API", "Server error body = $errorBody")
            }

            android.util.Log.e("API", "Signup error", e)

            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager.clearToken()
    }
}