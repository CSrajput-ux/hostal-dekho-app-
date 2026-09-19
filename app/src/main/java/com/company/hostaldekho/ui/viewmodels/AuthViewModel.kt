package com.company.hostaldekho.ui.viewmodels

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.hostaldekho.data.remote.dto.AuthResponse
import com.company.hostaldekho.data.remote.dto.UserRead
import com.company.hostaldekho.data.remote.dto.UserUpdateRequest
import com.company.hostaldekho.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class OtpSent(val phone: String) : AuthState()
    data class Success(val response: AuthResponse) : AuthState()
    data class Error(val message: String) : AuthState()
    object UpdateSuccess : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<UserRead?>(null)
    val currentUser: StateFlow<UserRead?> = _currentUser

    private val _profileUpdateState = MutableStateFlow<AuthState>(AuthState.Idle)
    val profileUpdateState: StateFlow<AuthState> = _profileUpdateState

    init {
        // Automatically fetch user profile if tokens exist
        if (isLoggedIn()) {
            fetchCurrentUser()
        }
    }

    /**
     * Perform Google Sign-In using AndroidX Credential Manager API.
     */
    fun performGoogleSignIn(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.signInWithGoogle(context)
            result.onSuccess { authResponse ->
                _authState.value = AuthState.Success(authResponse)
            }.onFailure { error ->
                _authState.value = AuthState.Error(
                    error.localizedMessage ?: "Google Sign-In failed. Please try again."
                )
            }
        }
    }

    /**
     * Login with credentials.
     */
    fun login(identifier: String, password: String) {
        if (identifier.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please enter both username/email and password")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.loginWithCredentials(identifier, password)
            result.onSuccess { authResponse ->
                _currentUser.value = authResponse.user
                _authState.value = AuthState.Success(authResponse)
            }.onFailure { error ->
                _authState.value = AuthState.Error(
                    error.localizedMessage ?: "Login failed. Please check your credentials."
                )
            }
        }
    }

    /**
     * Fetch user profile details.
     */
    fun fetchCurrentUser() {
        viewModelScope.launch {
            val result = repository.getCurrentUser()
            result.onSuccess { user ->
                _currentUser.value = user
            }
        }
    }

    /**
     * Update user profile details.
     */
    fun updateProfile(name: String, email: String, mobile: String, gender: String) {
        viewModelScope.launch {
            _profileUpdateState.value = AuthState.Loading
            val request = UserUpdateRequest(
                name = name,
                email = email,
                mobile = mobile,
                gender = gender
            )
            val result = repository.updateProfile(request)
            result.onSuccess { user ->
                _currentUser.value = user
                _profileUpdateState.value = AuthState.UpdateSuccess
            }.onFailure { error ->
                _profileUpdateState.value = AuthState.Error(error.localizedMessage ?: "Failed to update profile")
            }
        }
    }

    /**
     * Send OTP via Firebase Phone Auth.
     */
    fun sendOtp(phone: String, activity: Activity) {
        val cleanPhone = phone.trim()
        if (cleanPhone.length != 10 || !cleanPhone.all { it.isDigit() }) {
            _authState.value = AuthState.Error("Please enter a valid 10-digit mobile number")
            return
        }

        _authState.value = AuthState.Loading
        repository.sendFirebasePhoneOtp(
            activity = activity,
            phone = cleanPhone,
            onCodeSent = { verId ->
                _authState.value = AuthState.OtpSent(phone = cleanPhone)
            },
            onAutoVerified = { authResponse ->
                _authState.value = AuthState.Success(authResponse)
            },
            onError = { errorMsg ->
                _authState.value = AuthState.Error(errorMsg)
            }
        )
    }

    /**
     * Verify user-entered 6-digit OTP code against Firebase and exchange with FastAPI.
     */
    fun verifyOtp(code: String) {
        val cleanCode = code.trim()
        if (cleanCode.length != 6 || !cleanCode.all { it.isDigit() }) {
            _authState.value = AuthState.Error("Please enter the complete 6-digit OTP")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.verifyFirebaseOtp(cleanCode)
            result.onSuccess { authResponse ->
                _authState.value = AuthState.Success(authResponse)
            }.onFailure { error ->
                _authState.value = AuthState.Error(
                    error.localizedMessage ?: "Invalid OTP code. Please try again."
                )
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun isLoggedIn(): Boolean = repository.isLoggedIn()
}
