package com.company.hostaldekho.data.remote

import com.company.hostaldekho.data.remote.dto.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Request body for POST /auth/register.
 * FIX: `name` is sent as `name` (maps to user's display name).
 * Backend accepts username, name, real_name, email, mobile, password.
 */
data class RegisterRequest(
    val username: String? = null,
    val name: String? = null,
    @com.google.gson.annotations.SerializedName("real_name") val realName: String? = null,
    val email: String,
    val mobile: String? = null,
    val password: String,
    val role: String = "STUDENT"
)

interface RegisterApi {
    /**
     * FIX: Backend returns AuthResponse (access_token, refresh_token, user),
     * NOT a custom RegisterResponse. Using AuthResponse directly.
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}
