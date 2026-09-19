package com.company.hostaldekho.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.company.hostaldekho.BuildConfig
import com.company.hostaldekho.data.local.TokenManager
import com.company.hostaldekho.data.remote.HostelDekhoApiService
import com.company.hostaldekho.data.remote.dto.AuthResponse
import com.company.hostaldekho.data.remote.dto.GoogleAuthRequest
import com.company.hostaldekho.data.remote.dto.RefreshRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthRepository"

@Singleton
class AuthRepository @Inject constructor(
    private val api: HostelDekhoApiService,
    private val tokenManager: TokenManager
) {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    /**
     * Google Sign-In using AndroidX Credential Manager API.
     */
    suspend fun signInWithGoogle(context: Context): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)
            val webClientId = BuildConfig.WEB_CLIENT_ID

            Log.d(TAG, "Initiating Google Sign-In with Web Client ID: $webClientId")

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val googleIdToken = googleIdTokenCredential.idToken

            Log.d(TAG, "Successfully obtained Google ID Token. Authenticating with Firebase...")

            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user
                ?: return@withContext Result.failure(IllegalStateException("Firebase Google Sign-In failed: User is null."))

            val tokenResult = user.getIdToken(true).await()
            val firebaseIdToken = tokenResult.token
                ?: return@withContext Result.failure(IllegalStateException("Failed to retrieve Firebase ID Token."))

            Log.d(TAG, "Obtained Firebase ID Token. Exchanging with FastAPI Backend...")
            verifyTokenWithBackend(firebaseIdToken)
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-In cancelled by user.")
            Result.failure(Exception("Google Sign-In was cancelled."))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "CredentialManager error: ${e.message}", e)
            Result.failure(Exception("Google Sign-In failed: ${e.localizedMessage}"))
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In exception: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /**
     * Initiate Firebase Phone Authentication.
     */
    fun sendFirebasePhoneOtp(
        activity: Activity,
        phone: String,
        onCodeSent: (String) -> Unit,
        onAutoVerified: (AuthResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        val formattedPhone = if (phone.startsWith("+")) phone else "+91$phone"
        Log.d(TAG, "Initiating Firebase Phone Auth for: $formattedPhone")

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "Firebase auto-verification completed successfully.")
                signInWithFirebaseCredential(credential, onAutoVerified, onError)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "Firebase Phone Verification Failed: ${e.localizedMessage}", e)
                onError(e.localizedMessage ?: "Firebase Verification Failed")
            }

            override fun onCodeSent(
                verId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "Firebase OTP Code sent. VerificationId: $verId")
                verificationId = verId
                resendToken = token
                onCodeSent(verId)
            }
        }

        val optionsBuilder = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(formattedPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        resendToken?.let { optionsBuilder.setForceResendingToken(it) }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    /**
     * Verify 6-digit OTP code against Firebase, retrieve Firebase ID Token, and send to FastAPI.
     */
    suspend fun verifyFirebaseOtp(code: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        val currentVerId = verificationId
            ?: return@withContext Result.failure(IllegalStateException("No verification session found. Request OTP again."))

        try {
            val credential = PhoneAuthProvider.getCredential(currentVerId, code)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user
                ?: return@withContext Result.failure(IllegalStateException("Firebase Auth failed: user is null."))

            val tokenResult = user.getIdToken(true).await()
            val idToken = tokenResult.token
                ?: return@withContext Result.failure(IllegalStateException("Failed to extract Firebase ID token."))

            Log.d(TAG, "Obtained Firebase ID Token. Verifying with FastAPI backend...")
            verifyTokenWithBackend(idToken)
        } catch (e: Exception) {
            Log.e(TAG, "Error in verifyFirebaseOtp: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /**
     * Login using Username/Email and Password.
     */
    suspend fun loginWithCredentials(identifier: String, password: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(com.company.hostaldekho.data.remote.dto.LoginRequest(identifier, password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                Log.d(TAG, "Login successful for: $identifier")
                Result.success(authResponse)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Login failed (${response.code()})"
                Log.e(TAG, "Backend Error: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login Exception: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch the current user profile from the backend.
     */
    suspend fun getCurrentUser(): Result<com.company.hostaldekho.data.remote.dto.UserRead> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch user profile (${response.code()})"
                Log.e(TAG, "Backend Error: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Profile Fetch Exception: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /**
     * Update the current user profile.
     */
    suspend fun updateProfile(request: com.company.hostaldekho.data.remote.dto.UserUpdateRequest): Result<com.company.hostaldekho.data.remote.dto.UserRead> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateCurrentUser(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to update profile (${response.code()})"
                Log.e(TAG, "Backend Error: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Profile Update Exception: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    private fun signInWithFirebaseCredential(
        credential: PhoneAuthCredential,
        onSuccess: (AuthResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val idToken = tokenTask.result?.token
                            if (idToken != null) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val res = verifyTokenWithBackend(idToken)
                                    res.onSuccess { onSuccess(it) }
                                       .onFailure { onError(it.message ?: "Backend verification failed") }
                                }
                            } else {
                                onError("Failed to retrieve Firebase ID Token")
                            }
                        } else {
                            onError(tokenTask.exception?.localizedMessage ?: "Failed to get ID Token")
                        }
                    }
                } else {
                    onError(task.exception?.localizedMessage ?: "Firebase Sign-In failed")
                }
            }
    }

    /**
     * Send Firebase ID Token to FastAPI backend `POST /api/v1/auth/firebase`.
     */
    suspend fun verifyTokenWithBackend(idToken: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.firebaseLogin(GoogleAuthRequest(idToken = idToken))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                Log.d(TAG, "Backend successfully verified Firebase Token & returned JWT.")
                Result.success(authResponse)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Backend Token verification failed (${response.code()})"
                Log.e(TAG, "Backend Error: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Backend Exception: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /** Refresh tokens using stored refresh token. */
    suspend fun refreshTokens(): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val refreshToken = tokenManager.getRefreshToken()
                ?: return@withContext Result.failure(Exception("No refresh token stored"))
            val response = api.refreshToken(RefreshRequest(refreshToken = refreshToken))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                Result.success(authResponse)
            } else {
                tokenManager.clearTokens()
                Result.failure(Exception("Session expired. Please log in again."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean = !tokenManager.getAccessToken().isNullOrBlank()

    fun logout() {
        firebaseAuth.signOut()
        tokenManager.clearTokens()
    }
}
