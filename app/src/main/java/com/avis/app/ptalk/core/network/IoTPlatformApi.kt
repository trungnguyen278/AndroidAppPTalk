package com.avis.app.ptalk.core.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// --- Auth Requests & Responses ---

data class LoginRequest(
    val auth_username: String,
    val password: String
)

data class LoginResponse(
    val access_token: String,
    val token_type: String = "bearer",
    val user: UserDto
)

data class SignupRequest(
    val auth_username: String,
    val username: String,
    val password: String,
    val email: String? = null,
    val phone_number: String? = null
)

data class UserDto(
    val id: String,
    val email: String?,
    val phone_number: String?,
    val username: String?
)

// --- Device Requests & Responses ---

data class DeviceDto(
    val id: String, // Note: This is CloudIoTPlatform's 24-char generated ID
    val label: String,
    val mac_address: String, // This is the eFuse MAC we need for MQTT!
    val is_active: Boolean,
    val user_id: String,
    val product_id: String?
)

interface IoTPlatformApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/v1/auth/signup")
    suspend fun signup(@Body request: SignupRequest): UserDto

    @GET("api/v1/devices")
    suspend fun getMyDevices(): List<DeviceDto>
}
