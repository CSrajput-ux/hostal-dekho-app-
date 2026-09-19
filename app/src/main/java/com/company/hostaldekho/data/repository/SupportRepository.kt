package com.company.hostaldekho.data.repository

import com.company.hostaldekho.data.remote.HostelDekhoApiService
import com.company.hostaldekho.data.remote.dto.SupportTicketCreateRequest
import com.company.hostaldekho.data.remote.dto.SupportTicketResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupportRepository @Inject constructor(
    private val api: HostelDekhoApiService
) {

    suspend fun getMySupportTickets(): Result<List<SupportTicketResponse>> {
        return try {
            val response = api.getMySupportTickets()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load tickets (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTicket(subject: String, description: String): Result<SupportTicketResponse> {
        return try {
            val request = SupportTicketCreateRequest(subject = subject, description = description)
            val response = api.createSupportTicket(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string() ?: "Failed to submit complaint (HTTP ${response.code()})"
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
