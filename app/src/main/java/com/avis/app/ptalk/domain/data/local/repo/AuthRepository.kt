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
            android.util.Log.d("API", "Login request username=$username")

            val response = api.login(LoginRequest(username, password))

            android.util.Log.d("API", "Login success, got tokens")

            // Extract userId from JWT 'sub' claim
            val userId = extractUserIdFromJwt(response.access_token)
            android.util.Log.d("API", "UserID from JWT=$userId")

            // Save tokens
            tokenManager.saveToken(response.access_token, response.refresh_token, userId)
            // Save username from login form as fallback
            tokenManager.saveUserInfo(username = username, email = null, phone = null)

            // Fetch full user profile from /api/v1/me
            try {
                val userInfo = api.getMe()
                android.util.Log.d("API", "User info: $userInfo")
                tokenManager.saveUserInfo(
                    username = userInfo.username,
                    email = userInfo.email,
                    phone = userInfo.phone_number
                )
            } catch (e: Exception) {
                android.util.Log.w("API", "Could not fetch user profile, using login username", e)
            }

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

    private fun extractUserIdFromJwt(token: String): String {
        return try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payload = android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING)
                val json = String(payload, Charsets.UTF_8)
                val jsonObj = org.json.JSONObject(json)
                jsonObj.optString("sub", "")
            } else ""
        } catch (e: Exception) {
            android.util.Log.e("API", "JWT decode error", e)
            ""
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

            // Signup also returns TokenResponse
            val userId = extractUserIdFromJwt(response.access_token)
            tokenManager.saveToken(response.access_token, response.refresh_token, userId)
            tokenManager.saveUserInfo(
                username = username,
                email = email,
                phone = phone
            )

            Result.success(Unit)

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

    fun getUsername(): String? = tokenManager.getUsername()
    fun getEmail(): String? = tokenManager.getEmail()
    fun getPhone(): String? = tokenManager.getPhone()
    fun getUserId(): String? = tokenManager.getUserId()
}