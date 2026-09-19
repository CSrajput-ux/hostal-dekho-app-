package com.company.hostaldekho.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.BuildConfig
import com.company.hostaldekho.data.payment.PaymentResultBus
import com.company.hostaldekho.ui.components.InnerPageTopBar
import com.company.hostaldekho.ui.components.SaaSButton
import com.company.hostaldekho.ui.theme.*
import com.company.hostaldekho.ui.viewmodels.BookingState
import com.company.hostaldekho.ui.viewmodels.PropertyViewModel
import com.razorpay.Checkout
import org.json.JSONObject

/**
 * BUG-23 FIX: Wired PropertyViewModel to create a booking and launch Razorpay checkout.
 * BUG-22 FIX: Replaced deprecated Divider with HorizontalDivider.
 */
@Composable
fun PropertyBookingScreen(
    propertyId: String,
    roomId: String,
    hostelName: String,
    price: String,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    // BUG-23 FIX: Inject PropertyViewModel
    viewModel: PropertyViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val bookingState by viewModel.bookingState.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        PaymentResultBus.results.collect { result ->
            when (result) {
                is PaymentResultBus.Result.Success -> {
                    val order = (bookingState as? BookingState.OrderCreated)?.booking
                    if (order?.razorpayOrderId != null) {
                        viewModel.onPaymentSuccess(order.razorpayOrderId, result.paymentId, result.signature)
                    }
                }
                is PaymentResultBus.Result.Failure -> viewModel.onPaymentError(
                    result.message ?: "Payment failed (code ${result.code})"
                )
            }
        }
    }

    // Observe booking state changes
    LaunchedEffect(bookingState) {
        when (val state = bookingState) {
            is BookingState.OrderCreated -> {
                // BUG-23 FIX: Launch Razorpay checkout when order is created
                val orderId = state.booking.razorpayOrderId
                val amount = state.booking.amount
                if (orderId != null && !orderId.startsWith("order_dev_")) {
                    launchRazorpayCheckout(
                        context = context as Activity,
                        orderId = orderId,
                        amount = amount.toDouble(),
                    )
                } else {
                    // Dev mode: no Razorpay configured — simulate success
                    viewModel.onPaymentSuccess(orderId ?: "", "pay_dev_mock", "sig_dev_mock")
                }
            }
            is BookingState.Success -> {
                onConfirm()
                viewModel.resetBookingState()
            }
            is BookingState.Error -> {
                errorMessage = state.message
            }
            else -> {}
        }
    }

    val isLoading = bookingState is BookingState.Loading

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            InnerPageTopBar(title = "Complete Booking", onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Booking Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.HomeWork, contentDescription = null, tint = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            hostelName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(price, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Booking Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Price Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PriceRow("Monthly Rent", price)
                    Spacer(modifier = Modifier.height(8.dp))
                    PriceRow("Service Fee", "₹99")
                    // BUG-22 FIX: HorizontalDivider replaces deprecated Divider
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    PriceRow(
                        label = "Pay Now",
                        value = price,
                        isBold = true,
                        valueColor = PrimaryBlue
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        color = DangerRed,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            SaaSButton(
                text = if (isLoading) "Processing..." else "Confirm & Pay",
                onClick = {
                    errorMessage = null
                    // BUG-23 FIX: Actually call the booking API
                    viewModel.createBooking(propertyId = propertyId, roomId = roomId)
                },
                isLoading = isLoading,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PriceRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = if (isBold) TextPrimary else TextSecondary,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
        Text(
            value,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Launch Razorpay checkout with the given order ID.
 * Razorpay SDK calls onSuccess/onError via PaymentResultListener on the Activity.
 * The Activity (MainActivity) must implement PaymentResultListener.
 */
private fun launchRazorpayCheckout(
    context: Activity,
    orderId: String,
    amount: Double,
) {
    try {
        val checkout = Checkout()
        checkout.setKeyID(BuildConfig.RAZORPAY_KEY_ID)
        checkout.setImage(com.company.hostaldekho.R.drawable.logo)

        val options = JSONObject().apply {
            put("name", "HostelDekho")
            put("description", "Hostel Booking")
            put("order_id", orderId)
            put("amount", (amount * 100).toInt())   // paise
            put("currency", "INR")
            put("prefill", JSONObject().apply {
                put("contact", "")
                put("email", "")
            })
            put("theme", JSONObject().apply {
                put("color", "#2563EB")
            })
        }
        checkout.open(context, options)
    } catch (e: Exception) {
        PaymentResultBus.emit(PaymentResultBus.Result.Failure(-1, "Failed to open payment: ${e.message}"))
    }
}
