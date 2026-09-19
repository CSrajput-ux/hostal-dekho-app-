package com.company.hostaldekho.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.hostaldekho.data.remote.dto.KycDocumentResponse
import com.company.hostaldekho.data.repository.KycRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class KycState {
    object Idle : KycState()
    object Loading : KycState()
    data class Loaded(val documents: List<KycDocumentResponse>) : KycState()
    object UploadSuccess : KycState()
    data class Error(val message: String) : KycState()
}

@HiltViewModel
class KycViewModel @Inject constructor(
    private val repository: KycRepository
) : ViewModel() {

    private val _kycState = MutableStateFlow<KycState>(KycState.Idle)
    val kycState: StateFlow<KycState> = _kycState

    init {
        loadDocuments()
    }

    fun loadDocuments() {
        viewModelScope.launch {
            _kycState.value = KycState.Loading
            val result = repository.getMyKycDocuments()
            result.onSuccess { docs ->
                _kycState.value = KycState.Loaded(docs)
            }.onFailure { error ->
                _kycState.value = KycState.Error(error.message ?: "Failed to load KYC documents")
            }
        }
    }

    fun uploadDocument(documentType: String, documentUrl: String) {
        if (documentType.isBlank() || documentUrl.isBlank()) {
            _kycState.value = KycState.Error("Document type and URL are required")
            return
        }
        viewModelScope.launch {
            _kycState.value = KycState.Loading
            val result = repository.uploadKycDocument(documentType, documentUrl)
            result.onSuccess {
                _kycState.value = KycState.UploadSuccess
                loadDocuments()
            }.onFailure { error ->
                _kycState.value = KycState.Error(error.message ?: "Failed to upload document")
            }
        }
    }

    fun resetState() {
        loadDocuments()
    }
}
