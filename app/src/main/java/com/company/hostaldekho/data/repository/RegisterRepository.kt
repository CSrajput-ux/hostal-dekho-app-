package com.company.hostaldekho.data.repository

import android.util.Log
import com.company.hostaldekho.data.local.TokenManager
import com.company.hostaldekho.data.remote.RegisterApi
import com.company.hostaldekho.data.remote.RegisterRequest
import com.company.hostaldekho.data.remote.dto.AuthResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RegisterRepository"

@Singleton
class RegisterRepository @Inject constructor(
    private val api: RegisterApi,
    private val tokenManager: TokenManager
) {
    /**
     * FIX: Backend returns AuthResponse (not a custom RegisterResponse).
     * We now save tokens from the AuthResponse directly.
     */
    suspend fun register(request: RegisterRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.register(request)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                // Save JWT tokens so user is immediately logged in after registration
                tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                Log.d(TAG, "Registration successful for: ${request.email}, user ID: ${authResponse.user.id}")
                Result.success(authResponse)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Registration failed (${response.code()})"
                Log.e(TAG, "Backend Error: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Registration Exception: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }
}
