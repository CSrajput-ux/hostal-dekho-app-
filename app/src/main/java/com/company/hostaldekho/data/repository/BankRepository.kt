package com.company.hostaldekho.data.repository

import com.company.hostaldekho.data.remote.HostelDekhoApiService
import com.company.hostaldekho.data.remote.dto.BankAccountCreateRequest
import com.company.hostaldekho.data.remote.dto.BankAccountResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankRepository @Inject constructor(
    private val api: HostelDekhoApiService
) {

    suspend fun getMyBankAccounts(): Result<List<BankAccountResponse>> {
        return try {
            val response = api.getMyBankAccounts()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load bank accounts (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addBankAccount(
        accountHolderName: String,
        accountNumber: String,
        ifscCode: String,
        bankName: String,
        isPrimary: Boolean = true
    ): Result<BankAccountResponse> {
        return try {
            val request = BankAccountCreateRequest(
                accountHolderName = accountHolderName,
                accountNumber = accountNumber,
                ifscCode = ifscCode,
                bankName = bankName,
                isPrimary = isPrimary
            )
            val response = api.addBankAccount(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string() ?: "Failed to save bank account (HTTP ${response.code()})"
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
