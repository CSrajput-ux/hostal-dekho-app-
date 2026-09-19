package com.company.hostaldekho.model

import androidx.compose.ui.graphics.Color
import com.company.hostaldekho.ui.theme.SuccessGreen
import com.company.hostaldekho.ui.theme.WarningOrange
import com.company.hostaldekho.ui.theme.DangerRed

/**
 * UI-layer booking status enum for display in Owner/Student screens.
 * FIX: Added ACTIVE status to match backend BookingStatus.ACTIVE.
 * Note: CONFIRMED is kept as alias for ACTIVE for backward-compat in UI.
 */
enum class BookingStatus(val displayName: String, val color: Color) {
    PENDING("Pending Payment", WarningOrange),
    ACTIVE("Active", SuccessGreen),
    CONFIRMED("Confirmed", SuccessGreen),   // UI alias for ACTIVE (same color)
    COMPLETED("Completed", Color(0xFF64748B)),
    CANCELLED("Cancelled", DangerRed)
}

/**
 * Maps backend status string to UI BookingStatus enum for display.
 */
fun String.toUiBookingStatus(): BookingStatus = when (this.uppercase()) {
    "PENDING_PAYMENT", "PENDING" -> BookingStatus.PENDING
    "ACTIVE"                     -> BookingStatus.ACTIVE
    "COMPLETED"                  -> BookingStatus.COMPLETED
    "CANCELLED"                  -> BookingStatus.CANCELLED
    else                         -> BookingStatus.PENDING
}

data class Booking(
    val id: String,
    val customerName: String,
    val roomType: String,
    val checkInDate: String,
    val duration: String,
    val amount: String,
    val status: BookingStatus,
    val paymentStatus: String = "Paid"
)
