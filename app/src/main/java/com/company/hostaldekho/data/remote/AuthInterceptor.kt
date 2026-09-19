package com.company.hostaldekho.data.remote

import com.company.hostaldekho.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    // Paths that do NOT require an Authorization header
    private val unauthenticatedPaths = listOf(
        "/auth/send-otp",
        "/auth/verify-otp",
        "/auth/login",          // Email/password login
        "/auth/signin",         // Alias for login
        "/auth/token",          // OAuth2 token endpoint alias
        "/auth/register",       // FIX: Registration was missing — caused 401
        "/auth/signup",         // Alias for register
        "/auth/create-account", // Another alias for register
        "/auth/firebase",       // Google Sign-In / Phone OTP via Firebase
        "/auth/refresh",        // Refresh must be unauthenticated (no valid token yet)
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val encodedPath = originalRequest.url.encodedPath

        // Skip auth header for public endpoints
        if (unauthenticatedPaths.any { encodedPath.contains(it) }) {
            return chain.proceed(originalRequest)
        }

        val accessToken = tokenManager.getAccessToken()
        val requestBuilder = originalRequest.newBuilder()

        if (!accessToken.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
        }

        val response = chain.proceed(requestBuilder.build())

        // On 401: clear tokens so user is redirected to login
        // A full implementation would attempt token refresh here synchronously.
        if (response.code == 401) {
            tokenManager.clearTokens()
        }

        return response
    }
}
