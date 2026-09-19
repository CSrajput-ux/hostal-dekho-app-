package com.company.hostaldekho.data.repository

import com.company.hostaldekho.data.remote.HostelDekhoApiService
import com.company.hostaldekho.data.remote.dto.PropertySearchItem
import com.company.hostaldekho.data.remote.dto.WishlistResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishlistRepository @Inject constructor(
    private val api: HostelDekhoApiService
) {

    suspend fun getMyWishlist(): Result<List<PropertySearchItem>> {
        return try {
            val response = api.getMyWishlist()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load wishlist (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addToWishlist(propertyId: String): Result<WishlistResponse> {
        return try {
            val response = api.addToWishlist(propertyId)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to add to wishlist (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromWishlist(propertyId: String): Result<Unit> {
        return try {
            val response = api.removeFromWishlist(propertyId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to remove from wishlist (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
