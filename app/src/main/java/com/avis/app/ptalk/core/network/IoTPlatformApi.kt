package com.avis.app.ptalk.core.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// --- Auth Requests & Responses ---

data class LoginRequest(
    val auth_username: String,
    val password: String
)

// Server returns TokenResponse for both login and signup
data class TokenResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String = "bearer"
)

data class RefreshRequest(
    val refresh_token: String
)

data class SignupRequest(
    val auth_username: String,
    val username: String,
    val password: String,
    val email: String? = null,
    val phone_number: String? = null
)

// --- User ---

// Response from GET /api/v1/me
data class UserRead(
    val userId: String,
    val username: String,
    val email: String,
    val phone_number: String? = null
)

// --- Device Requests & Responses ---

data class CreateDeviceRequest(
    val label: String? = null,
    val productId: String? = null,
    val macAddress: String,
    val compatibleAppVersion: String? = null,
    val firmwareVersion: String? = null,
    val buildNumber: String? = null,
    val deviceType: Int? = null,
    val connectionType: Int? = null,
    val model: String? = null
)

data class DeviceResponse(
    val id: String,
    val label: String? = null,
    val userId: String,
    val productId: String? = null
)

interface IoTPlatformApi {
    // Auth
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @POST("api/v1/auth/signup")
    suspend fun signup(@Body request: SignupRequest): TokenResponse

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): TokenResponse

    // User
    @GET("api/v1/me")
    suspend fun getMe(): UserRead

    // Devices
    @POST("api/v1/devices")
    suspend fun createDevice(@Body request: CreateDeviceRequest): DeviceResponse

    @GET("api/v1/devices/{device_id}")
    suspend fun getDevice(@Path("device_id") deviceId: String): DeviceResponse

    @PUT("api/v1/devices/{device_id}")
    suspend fun updateDevice(
        @Path("device_id") deviceId: String,
        @Body request: CreateDeviceRequest
    ): DeviceResponse

    @DELETE("api/v1/devices/{device_id}")
    suspend fun deleteDevice(@Path("device_id") deviceId: String)
}
