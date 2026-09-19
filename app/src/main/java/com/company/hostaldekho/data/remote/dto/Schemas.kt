package com.company.hostaldekho.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

// ─────────────────────────────────────────────────────────────────────────────
// ENUMS — match backend Python enum values exactly (UPPERCASE)
// ─────────────────────────────────────────────────────────────────────────────

enum class UserRole {
    @SerializedName("STUDENT") STUDENT,
    @SerializedName("OWNER") OWNER,
    @SerializedName("ADMIN") ADMIN
}

enum class KycStatus {
    @SerializedName("PENDING") PENDING,
    @SerializedName("UNDER_REVIEW") UNDER_REVIEW,
    @SerializedName("VERIFIED") VERIFIED,
    @SerializedName("REJECTED") REJECTED
}

enum class PropertyType {
    @SerializedName("HOSTEL") HOSTEL,
    @SerializedName("PG") PG,
    @SerializedName("FLAT") FLAT
}

enum class GenderAllowed {
    @SerializedName("BOYS") BOYS,
    @SerializedName("GIRLS") GIRLS,
    @SerializedName("UNISEX") UNISEX
}

enum class BookingStatus {
    // FIX: Backend now stores "PENDING_PAYMENT" (was "PENDING")
    @SerializedName("PENDING_PAYMENT") PENDING_PAYMENT,
    @SerializedName("ACTIVE") ACTIVE,
    @SerializedName("CANCELLED") CANCELLED,
    @SerializedName("COMPLETED") COMPLETED
}

enum class PaymentStatus {
    @SerializedName("Pending") PENDING,
    @SerializedName("Success") SUCCESS,
    @SerializedName("Failed") FAILED
}

// ─────────────────────────────────────────────────────────────────────────────
// AUTH SCHEMAS
// All field names match backend JSON output exactly (snake_case).
// ─────────────────────────────────────────────────────────────────────────────

/** Request body for POST /auth/send-otp */
data class SendOtpRequest(
    val mobile: String
)

/** Response from POST /auth/send-otp */
data class SendOtpResponse(
    val detail: String,
    @SerializedName("expires_in_seconds") val expiresInSeconds: Int,
    @SerializedName("debug_otp") val debugOtp: String? = null
)

/** Request body for POST /auth/verify-otp */
data class VerifyOtpRequest(
    val mobile: String,
    val code: String,
    val name: String? = null,
    val email: String? = null
)

/** Request body for POST /auth/firebase (Google Sign-In) */
data class GoogleAuthRequest(
    @SerializedName("id_token") val idToken: String
)

/** Request body for POST /auth/refresh */
data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

/**
 * Request body for POST /auth/login.
 * FIX: Backend LoginRequest accepts `login_id` as generic identifier field.
 * We send the user's input in `login_id` so backend can find by username/email/mobile.
 */
data class LoginRequest(
    @SerializedName("login_id") val loginId: String,
    val password: String
)

/**
 * Response from /auth/verify-otp, /auth/firebase, /auth/refresh, /auth/login, /auth/register.
 * FIX: Backend now sends snake_case field names (access_token, refresh_token).
 */
data class UserRead(
    val id: String,
    val name: String?,
    val email: String?,
    val mobile: String?,
    val username: String?,
    val role: UserRole,
    @SerializedName("profile_image") val profileImage: String?,
    @SerializedName("kyc_status") val kycStatus: KycStatus,
    @SerializedName("auth_provider") val authProvider: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("last_login_at") val lastLoginAt: String? = null
)

/**
 * Request body for PUT /auth/me (Update Profile)
 */
data class UserUpdateRequest(
    val name: String? = null,
    val email: String? = null,
    val mobile: String? = null,
    val username: String? = null,
    val gender: String? = null
)

data class AuthResponse(
    // FIX: Backend sends snake_case — access_token, refresh_token
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String,
    val user: UserRead
)

// ─────────────────────────────────────────────────────────────────────────────
// PROPERTY SCHEMAS
// ─────────────────────────────────────────────────────────────────────────────

/** A room within a property. */
data class RoomRead(
    val id: String,
    @SerializedName("room_type") val roomType: String,
    val price: BigDecimal,
    @SerializedName("availability_count") val availabilityCount: Int,
    val images: List<String> = emptyList()
)

/** Request to create a room (sent as part of PropertyCreateRequest). */
data class RoomCreateRequest(
    @SerializedName("room_type") val roomType: String,    // "Single", "Double", "Triple"
    val price: BigDecimal,
    @SerializedName("availability_count") val availabilityCount: Int = 0,
    val images: List<String> = emptyList()
)

/** Request body for POST /api/v1/properties/ — owner creates a new property. */
data class PropertyCreateRequest(
    val name: String,
    val description: String? = null,
    val address: String,
    val city: String,
    val state: String,
    val pincode: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val type: String,                       // "HOSTEL", "PG", "FLAT"
    val gender: String? = "UNISEX",         // "BOYS", "GIRLS", "UNISEX"
    val amenities: Map<String, Boolean>? = null,
    val images: List<String> = emptyList(),
    val rooms: List<RoomCreateRequest> = emptyList()
)

/**
 * Full property detail response from GET /properties/{id} and POST /properties/.
 * Includes gender, images, rating fields.
 */
data class PropertyRead(
    val id: String,
    @SerializedName("owner_id") val ownerId: String,
    val name: String,
    val description: String?,
    val address: String,
    val city: String,
    val state: String?,
    val pincode: String?,
    val latitude: Double?,
    val longitude: Double?,
    val type: PropertyType,
    val gender: GenderAllowed?,                             // NEW
    @SerializedName("verified_badge") val verifiedBadge: Boolean,
    val amenities: Map<String, Any>?,
    val images: List<String> = emptyList(),                 // NEW: property-level photos
    val rating: Float? = null,                              // NEW: average rating
    @SerializedName("is_active") val isActive: Boolean = true,
    val rooms: List<RoomRead>?,
    @SerializedName("created_at") val createdAt: String
)

/**
 * A single item in the property search results list.
 * Includes gender, images, rating for display in listing cards.
 */
data class PropertySearchItem(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val latitude: Double?,
    val longitude: Double?,
    val type: PropertyType,
    val gender: GenderAllowed?,                             // NEW
    @SerializedName("verified_badge") val verifiedBadge: Boolean,
    @SerializedName("starting_price") val startingPrice: BigDecimal?,
    @SerializedName("distance_km") val distanceKm: Double?,
    val images: List<String> = emptyList(),                 // NEW: for card display
    val rating: Float? = null                               // NEW: average rating
)

/** Top-level response from GET /properties/nearby and GET /properties/search. */
data class PropertySearchResponse(
    val total: Int,
    val properties: List<PropertySearchItem>
)

/** Single item in owner's property listing (from GET /properties/my). */
data class OwnerPropertyItem(
    val id: String,
    val name: String,
    val city: String,
    val type: PropertyType,
    val gender: GenderAllowed?,
    @SerializedName("verified_badge") val verifiedBadge: Boolean,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("total_rooms") val totalRooms: Int,
    @SerializedName("available_beds") val availableBeds: Int,
    @SerializedName("starting_price") val startingPrice: BigDecimal?,
    val images: List<String> = emptyList(),
    val rating: Float? = null,
    @SerializedName("created_at") val createdAt: String
)

data class OwnerPropertyListResponse(
    val total: Int,
    val properties: List<OwnerPropertyItem>
)

// ─────────────────────────────────────────────────────────────────────────────
// BOOKING SCHEMAS
// ─────────────────────────────────────────────────────────────────────────────

data class BookingCreateRequest(
    @SerializedName("property_id") val propertyId: String,
    @SerializedName("room_id") val roomId: String,
    @SerializedName("check_in_date") val checkInDate: String? = null,   // "YYYY-MM-DD"
    @SerializedName("duration_months") val durationMonths: Int = 1
)

data class BookingResponse(
    val id: String,
    val status: String,
    @SerializedName("razorpay_order_id") val razorpayOrderId: String?,
    val amount: BigDecimal
)

/** Single booking item for student's booking history (GET /bookings/my). */
data class MyBookingItem(
    val id: String,
    @SerializedName("property_id") val propertyId: String,
    @SerializedName("property_name") val propertyName: String,
    @SerializedName("property_address") val propertyAddress: String,
    @SerializedName("room_type") val roomType: String,
    val status: String,
    @SerializedName("amount_paid") val amountPaid: BigDecimal?,
    @SerializedName("check_in_date") val checkInDate: String?,
    @SerializedName("duration_months") val durationMonths: Int?,
    @SerializedName("created_at") val createdAt: String
)

data class MyBookingsResponse(
    val total: Int,
    val bookings: List<MyBookingItem>
)

/** Single booking item for owner's bookings dashboard (GET /bookings/owner). */
data class OwnerBookingItem(
    val id: String,
    @SerializedName("customer_name") val customerName: String?,
    @SerializedName("customer_mobile") val customerMobile: String?,
    @SerializedName("property_name") val propertyName: String,
    @SerializedName("room_type") val roomType: String,
    val status: String,
    @SerializedName("amount_paid") val amountPaid: BigDecimal?,
    @SerializedName("check_in_date") val checkInDate: String?,
    @SerializedName("duration_months") val durationMonths: Int?,
    @SerializedName("created_at") val createdAt: String
)

data class OwnerBookingsResponse(
    val total: Int,
    val bookings: List<OwnerBookingItem>
)

/** Owner dashboard summary stats (GET /api/v1/owner/stats). */
data class OwnerStatsResponse(
    @SerializedName("total_properties") val totalProperties: Int,
    @SerializedName("active_properties") val activeProperties: Int,
    @SerializedName("total_bookings") val totalBookings: Int,
    @SerializedName("active_bookings") val activeBookings: Int,
    @SerializedName("total_earnings") val totalEarnings: BigDecimal,
    @SerializedName("monthly_earnings") val monthlyEarnings: BigDecimal
)

// ─────────────────────────────────────────────────────────────────────────────
// PAYMENT SCHEMAS
// ─────────────────────────────────────────────────────────────────────────────

data class PaymentVerifyRequest(
    @SerializedName("razorpay_order_id") val razorpayOrderId: String,
    @SerializedName("razorpay_payment_id") val razorpayPaymentId: String,
    @SerializedName("razorpay_signature") val razorpaySignature: String
)

data class PaymentVerifyResponse(
    val success: Boolean,
    val message: String
)

// ─────────────────────────────────────────────────────────────────────────────
// BANK ACCOUNT SCHEMAS
// ─────────────────────────────────────────────────────────────────────────────

/** Request body for POST /bank/ */
data class BankAccountCreateRequest(
    @SerializedName("account_holder_name") val accountHolderName: String,
    @SerializedName("account_number") val accountNumber: String,
    @SerializedName("ifsc_code") val ifscCode: String,
    @SerializedName("bank_name") val bankName: String,
    @SerializedName("is_primary") val isPrimary: Boolean = true
)

/** Response from POST /bank/ and items in GET /bank/my */
data class BankAccountResponse(
    val id: String,
    @SerializedName("account_holder_name") val accountHolderName: String,
    @SerializedName("account_number") val accountNumber: String,
    @SerializedName("ifsc_code") val ifscCode: String,
    @SerializedName("bank_name") val bankName: String,
    @SerializedName("is_primary") val isPrimary: Boolean,
    @SerializedName("created_at") val createdAt: String
)

// ─────────────────────────────────────────────────────────────────────────────
// KYC SCHEMAS
// ─────────────────────────────────────────────────────────────────────────────

/** Request body for POST /kyc/ */
data class KycDocumentCreateRequest(
    @SerializedName("document_type") val documentType: String,  // AADHAAR, PAN, DRIVING_LICENSE
    @SerializedName("document_url") val documentUrl: String
)

/** Response from GET /kyc/my and POST /kyc/ */
data class KycDocumentResponse(
    val id: String,
    @SerializedName("document_type") val documentType: String,
    @SerializedName("document_url") val documentUrl: String,
    val status: String,  // PENDING, UNDER_REVIEW, VERIFIED, REJECTED
    @SerializedName("uploaded_at") val uploadedAt: String
)

// ─────────────────────────────────────────────────────────────────────────────
// SUPPORT TICKET SCHEMAS
// ─────────────────────────────────────────────────────────────────────────────

/** Request body for POST /support/ */
data class SupportTicketCreateRequest(
    val subject: String,
    val description: String
)

/** Response from GET /support/my and POST /support/ */
data class SupportTicketResponse(
    val id: String,
    val subject: String,
    val description: String,
    val status: String,  // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

// ─────────────────────────────────────────────────────────────────────────────
// WISHLIST SCHEMAS
// ─────────────────────────────────────────────────────────────────────────────

/** Response from POST /wishlists/{property_id} */
data class WishlistResponse(
    @SerializedName("user_id") val userId: String,
    @SerializedName("property_id") val propertyId: String,
    @SerializedName("created_at") val createdAt: String
)
