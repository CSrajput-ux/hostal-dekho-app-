package com.company.hostaldekho.data.remote

import com.company.hostaldekho.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface HostelDekhoApiService {

    // ──────────────────────────────────────────────────────────────────────
    // AUTH
    // ──────────────────────────────────────────────────────────────────────

    /** Send OTP to a mobile number. Returns debug_otp in DEBUG_MODE. */
    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<SendOtpResponse>

    /** Verify OTP. Returns JWT tokens on success. */
    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>

    /** Refresh access token using a valid refresh token. */
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<AuthResponse>

    /**
     * Login with username/email/mobile and password.
     * FIX: LoginRequest now sends `login_id` field to match backend schema.
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    /** Login/register with a Firebase Google Sign-In ID token. */
    @POST("auth/firebase")
    suspend fun firebaseLogin(@Body request: GoogleAuthRequest): Response<AuthResponse>

    /**
     * Get the currently logged-in user's profile details.
     * Backend: GET /api/v1/auth/me
     */
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<UserRead>

    /**
     * Update the currently logged-in user's profile.
     * Backend: PUT /api/v1/auth/me
     */
    @PUT("auth/me")
    suspend fun updateCurrentUser(@Body request: UserUpdateRequest): Response<UserRead>

    // ──────────────────────────────────────────────────────────────────────
    // PROPERTIES
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Get properties near a location (lat/lng).
     * Backend: GET /api/v1/properties/nearby?lat=&lng=&radius=&limit=&gender=&type=
     */
    @GET("properties/nearby")
    suspend fun getNearbyProperties(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radius: Double = 5000.0,
        @Query("limit") limit: Int = 20,
        @Query("gender") gender: String? = null,
        @Query("type") type: String? = null
    ): Response<PropertySearchResponse>

    /**
     * Search properties by city/text with filters.
     * Backend: GET /api/v1/properties/search?location=&type=&gender=&limit=&page=
     */
    @GET("properties/search")
    suspend fun searchProperties(
        @Query("location") location: String? = null,
        @Query("type") type: String? = null,
        @Query("gender") gender: String? = null,
        @Query("min_price") minPrice: Double? = null,
        @Query("max_price") maxPrice: Double? = null,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1
    ): Response<PropertySearchResponse>

    /**
     * Get owner's own property listings (requires auth).
     * Backend: GET /api/v1/properties/my
     */
    @GET("properties/my")
    suspend fun getMyProperties(): Response<OwnerPropertyListResponse>

    /**
     * Get full details for a single property including rooms.
     * Backend: GET /api/v1/properties/{id}
     */
    @GET("properties/{id}")
    suspend fun getPropertyById(@Path("id") id: String): Response<PropertyRead>

    /**
     * Create a new property listing (owner auth required).
     * Backend: POST /api/v1/properties/
     */
    @POST("properties/")
    suspend fun createProperty(@Body request: PropertyCreateRequest): Response<PropertyRead>

    // ──────────────────────────────────────────────────────────────────────
    // BOOKINGS
    // ──────────────────────────────────────────────────────────────────────

    /** Create a booking and get a Razorpay order ID. Requires authentication. */
    @POST("bookings/")
    suspend fun createBooking(@Body request: BookingCreateRequest): Response<BookingResponse>

    /**
     * Get current student's booking history (requires auth).
     * Backend: GET /api/v1/bookings/my
     */
    @GET("bookings/my")
    suspend fun getMyBookings(): Response<MyBookingsResponse>

    /**
     * Get all bookings for owner's properties (owner auth required).
     * Backend: GET /api/v1/bookings/owner
     */
    @GET("bookings/owner")
    suspend fun getOwnerBookings(): Response<OwnerBookingsResponse>

    // ──────────────────────────────────────────────────────────────────────
    // PAYMENTS
    // ──────────────────────────────────────────────────────────────────────

    /** Verify a Razorpay payment signature after checkout. */
    @POST("payments/verify")
    suspend fun verifyPayment(@Body request: PaymentVerifyRequest): Response<PaymentVerifyResponse>

    // ──────────────────────────────────────────────────────────────────────
    // OWNER DASHBOARD
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Get owner's aggregated dashboard stats.
     * Backend: GET /api/v1/owner/stats
     */
    @GET("owner/stats")
    suspend fun getOwnerStats(): Response<OwnerStatsResponse>

    // ──────────────────────────────────────────────────────────────────────
    // BANK ACCOUNTS
    // ──────────────────────────────────────────────────────────────────────

    /** Save a new bank account. Owner auth required. */
    @POST("bank/")
    suspend fun addBankAccount(@Body request: BankAccountCreateRequest): Response<BankAccountResponse>

    /** Get all bank accounts for the current owner. */
    @GET("bank/my")
    suspend fun getMyBankAccounts(): Response<List<BankAccountResponse>>

    // ──────────────────────────────────────────────────────────────────────
    // KYC DOCUMENTS
    // ──────────────────────────────────────────────────────────────────────

    /** Upload a new KYC document. */
    @POST("kyc/")
    suspend fun uploadKycDocument(@Body request: KycDocumentCreateRequest): Response<KycDocumentResponse>

    /** Get all KYC documents for the current user. */
    @GET("kyc/my")
    suspend fun getMyKycDocuments(): Response<List<KycDocumentResponse>>

    // ──────────────────────────────────────────────────────────────────────
    // SUPPORT TICKETS
    // ──────────────────────────────────────────────────────────────────────

    /** Submit a new support/complaint ticket. */
    @POST("support/")
    suspend fun createSupportTicket(@Body request: SupportTicketCreateRequest): Response<SupportTicketResponse>

    /** Get all tickets submitted by the current user. */
    @GET("support/my")
    suspend fun getMySupportTickets(): Response<List<SupportTicketResponse>>

    // ──────────────────────────────────────────────────────────────────────
    // WISHLISTS
    // ──────────────────────────────────────────────────────────────────────

    /** Add a property to wishlist. */
    @POST("wishlists/{property_id}")
    suspend fun addToWishlist(@Path("property_id") propertyId: String): Response<WishlistResponse>

    /** Remove a property from wishlist. */
    @DELETE("wishlists/{property_id}")
    suspend fun removeFromWishlist(@Path("property_id") propertyId: String): Response<Unit>

    /** Get all wishlisted properties for current user as PropertySearchItem list. */
    @GET("wishlists/my")
    suspend fun getMyWishlist(): Response<List<PropertySearchItem>>
}
