package com.company.hostaldekho.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.hostaldekho.data.remote.dto.SupportTicketResponse
import com.company.hostaldekho.data.repository.SupportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SupportState {
    object Idle : SupportState()
    object Loading : SupportState()
    data class Loaded(val tickets: List<SupportTicketResponse>) : SupportState()
    data class SubmitSuccess(val ticket: SupportTicketResponse) : SupportState()
    data class Error(val message: String) : SupportState()
}

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val repository: SupportRepository
) : ViewModel() {

    private val _supportState = MutableStateFlow<SupportState>(SupportState.Idle)
    val supportState: StateFlow<SupportState> = _supportState

    fun submitComplaint(subject: String, description: String) {
        if (subject.isBlank()) {
            _supportState.value = SupportState.Error("Subject cannot be empty")
            return
        }
        if (description.isBlank()) {
            _supportState.value = SupportState.Error("Description cannot be empty")
            return
        }
        viewModelScope.launch {
            _supportState.value = SupportState.Loading
            val result = repository.createTicket(subject, description)
            result.onSuccess { ticket ->
                _supportState.value = SupportState.SubmitSuccess(ticket)
            }.onFailure { error ->
                _supportState.value = SupportState.Error(error.message ?: "Failed to submit complaint")
            }
        }
    }

    fun resetState() {
        _supportState.value = SupportState.Idle
    }
}
