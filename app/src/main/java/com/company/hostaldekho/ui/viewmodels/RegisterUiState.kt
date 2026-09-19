package com.company.hostaldekho.ui.viewmodels

import com.company.hostaldekho.data.remote.dto.AuthResponse

/**
 * FIX: RegisterResponse removed — backend returns AuthResponse on /auth/register.
 * Success now carries AuthResponse directly.
 */
sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val response: AuthResponse) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
