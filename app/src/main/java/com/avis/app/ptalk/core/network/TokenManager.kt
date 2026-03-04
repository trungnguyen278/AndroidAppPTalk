package com.avis.app.ptalk.core.network

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.Response

class TokenManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "ptalk_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // In-memory state flow for UI to react to login/logout
    private val _isLoggedIn = MutableStateFlow(hasToken())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun saveToken(token: String, userId: String) {
        prefs.edit { 
            putString(KEY_ACCESS_TOKEN, token) 
            putString(KEY_USER_ID, userId)
        }
        _isLoggedIn.value = true
    }

    fun getToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun clearToken() {
        prefs.edit { 
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_USER_ID)
        }
        _isLoggedIn.value = false
    }

    private fun hasToken(): Boolean {
        return !getToken().isNullOrBlank()
    }
}

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenManager.getToken()

        // Skip adding token to auth endpoints
        if (originalRequest.url.encodedPath.contains("/auth/login") || 
            originalRequest.url.encodedPath.contains("/auth/signup")) {
            return chain.proceed(originalRequest)
        }

        if (token != null) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            return chain.proceed(newRequest)
        }

        return chain.proceed(originalRequest)
    }
}
