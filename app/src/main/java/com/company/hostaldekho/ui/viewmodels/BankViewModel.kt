package com.company.hostaldekho.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.hostaldekho.data.remote.dto.BankAccountResponse
import com.company.hostaldekho.data.repository.BankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BankState {
    object Idle : BankState()
    object Loading : BankState()
    data class Loaded(val accounts: List<BankAccountResponse>) : BankState()
    object SaveSuccess : BankState()
    data class Error(val message: String) : BankState()
}

@HiltViewModel
class BankViewModel @Inject constructor(
    private val repository: BankRepository
) : ViewModel() {

    private val _bankState = MutableStateFlow<BankState>(BankState.Idle)
    val bankState: StateFlow<BankState> = _bankState

    init {
        loadBankAccounts()
    }

    fun loadBankAccounts() {
        viewModelScope.launch {
            _bankState.value = BankState.Loading
            val result = repository.getMyBankAccounts()
            result.onSuccess { accounts ->
                _bankState.value = BankState.Loaded(accounts)
            }.onFailure { error ->
                _bankState.value = BankState.Error(error.message ?: "Failed to load bank accounts")
            }
        }
    }

    fun saveBankAccount(
        accountHolderName: String,
        accountNumber: String,
        ifscCode: String,
        bankName: String,
        upiId: String = ""
    ) {
        if (accountHolderName.isBlank() || accountNumber.isBlank() || ifscCode.isBlank() || bankName.isBlank()) {
            _bankState.value = BankState.Error("Please fill all required fields")
            return
        }
        viewModelScope.launch {
            _bankState.value = BankState.Loading
            val result = repository.addBankAccount(
                accountHolderName = accountHolderName,
                accountNumber = accountNumber,
                ifscCode = ifscCode,
                bankName = bankName,
                isPrimary = true
            )
            result.onSuccess {
                _bankState.value = BankState.SaveSuccess
            }.onFailure { error ->
                _bankState.value = BankState.Error(error.message ?: "Failed to save bank account")
            }
        }
    }

    fun resetState() {
        loadBankAccounts()
    }
}
