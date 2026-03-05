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

data class CreateDeviceRequest(
    val label: String,
    val productId: String? = null,
    val macAddress: String,
    val compatibleAppVersion: String? = null,
    val firmwareVersion: String? = null,
    val buildNumber: String? = null,
    val deviceType: Int = 0,
    val connectionType: Int = 0,
    val model: String? = null
)

data class DeviceResponse(
    val id: String,
    val label: String,
    val userId: String,
    val productId: String?
)

interface IoTPlatformApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/v1/auth/signup")
    suspend fun signup(@Body request: SignupRequest): UserDto

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

