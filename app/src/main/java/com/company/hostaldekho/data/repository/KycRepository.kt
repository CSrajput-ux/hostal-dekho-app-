package com.company.hostaldekho.data.repository

import com.company.hostaldekho.data.remote.HostelDekhoApiService
import com.company.hostaldekho.data.remote.dto.KycDocumentCreateRequest
import com.company.hostaldekho.data.remote.dto.KycDocumentResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KycRepository @Inject constructor(
    private val api: HostelDekhoApiService
) {

    suspend fun getMyKycDocuments(): Result<List<KycDocumentResponse>> {
        return try {
            val response = api.getMyKycDocuments()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load KYC documents (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadKycDocument(
        documentType: String,
        documentUrl: String
    ): Result<KycDocumentResponse> {
        return try {
            val request = KycDocumentCreateRequest(
                documentType = documentType,
                documentUrl = documentUrl
            )
            val response = api.uploadKycDocument(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string() ?: "Failed to upload document (HTTP ${response.code()})"
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
