package com.company.hostaldekho.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.hostaldekho.data.remote.RegisterRequest
import com.company.hostaldekho.data.repository.RegisterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: RegisterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            val result = repository.register(request)
            result.onSuccess {
                _uiState.value = RegisterUiState.Success(it)
            }.onFailure {
                _uiState.value = RegisterUiState.Error(it.message ?: "An unexpected error occurred")
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}
