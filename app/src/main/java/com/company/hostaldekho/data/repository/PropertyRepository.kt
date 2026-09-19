package com.company.hostaldekho.data.repository

import com.company.hostaldekho.data.remote.HostelDekhoApiService
import com.company.hostaldekho.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PropertyRepository @Inject constructor(
    private val api: HostelDekhoApiService
) {

    // ─────────────────────────────────── PROPERTY SEARCH ────────────────────────

    suspend fun getNearbyProperties(
        latitude: Double,
        longitude: Double,
        radius: Double = 5000.0,
        limit: Int = 20,
        gender: String? = null,
        type: String? = null
    ): Result<PropertySearchResponse> {
        return try {
            val response = api.getNearbyProperties(latitude, longitude, radius, limit, gender, type)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load nearby properties (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchProperties(
        location: String? = null,
        type: String? = null,
        gender: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        limit: Int = 20
    ): Result<PropertySearchResponse> {
        return try {
            val response = api.searchProperties(
                location = location,
                type = type,
                gender = gender,
                minPrice = minPrice,
                maxPrice = maxPrice,
                limit = limit
            )
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to search properties (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPropertyById(id: String): Result<PropertyRead> {
        return try {
            val response = api.getPropertyById(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Property not found (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────── OWNER PROPERTIES ───────────────────────

    /**
     * Get owner's own property listings.
     * Called from OwnerDashboard 'My Listings' section.
     */
    suspend fun getMyProperties(): Result<OwnerPropertyListResponse> {
        return try {
            val response = api.getMyProperties()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load your properties (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a new property listing.
     * Called from AddPropertyScreen on submit.
     */
    suspend fun createProperty(request: PropertyCreateRequest): Result<PropertyRead> {
        return try {
            val response = api.createProperty(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string() ?: "Failed to create property (HTTP ${response.code()})"
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────── BOOKINGS ───────────────────────────────

    suspend fun createBooking(
        propertyId: String,
        roomId: String,
        checkInDate: String? = null,
        durationMonths: Int = 1
    ): Result<BookingResponse> {
        return try {
            val response = api.createBooking(
                BookingCreateRequest(
                    propertyId = propertyId,
                    roomId = roomId,
                    checkInDate = checkInDate,
                    durationMonths = durationMonths
                )
            )
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create booking (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current student's booking history.
     * Called from BookingsScreen.
     */
    suspend fun getMyBookings(): Result<MyBookingsResponse> {
        return try {
            val response = api.getMyBookings()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load bookings (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all bookings for owner's properties.
     * Called from OwnerBookingsScreen and OwnerDashboard.
     */
    suspend fun getOwnerBookings(): Result<OwnerBookingsResponse> {
        return try {
            val response = api.getOwnerBookings()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load owner bookings (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────── PAYMENTS ───────────────────────────────

    suspend fun verifyPayment(
        orderId: String,
        paymentId: String,
        signature: String
    ): Result<Boolean> {
        return try {
            val response = api.verifyPayment(
                PaymentVerifyRequest(
                    razorpayOrderId = orderId,
                    razorpayPaymentId = paymentId,
                    razorpaySignature = signature
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Payment verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────── OWNER STATS ───────────────────────────

    /**
     * Get owner's aggregated dashboard stats.
     * Called from OwnerDashboardScreen stats cards.
     */
    suspend fun getOwnerStats(): Result<OwnerStatsResponse> {
        return try {
            val response = api.getOwnerStats()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load dashboard stats (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
