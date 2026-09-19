package com.company.hostaldekho.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.ui.components.InnerPageTopBar
import com.company.hostaldekho.ui.components.SettingsGroup
import com.company.hostaldekho.ui.components.SettingsItem
import com.company.hostaldekho.ui.theme.*
import com.company.hostaldekho.ui.viewmodels.MyBookingsState
import com.company.hostaldekho.ui.viewmodels.PropertyViewModel
import java.math.BigDecimal

@Composable
fun PaymentRefundScreen(
    onBack: () -> Unit,
    propertyViewModel: PropertyViewModel = hiltViewModel()
) {
    val myBookingsState by propertyViewModel.myBookingsState.collectAsState()

    LaunchedEffect(Unit) {
        propertyViewModel.loadMyBookings()
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            InnerPageTopBar(title = "Payment & Refund", onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            when (val state = myBookingsState) {
                is MyBookingsState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is MyBookingsState.Success -> {
                    val bookings = state.data.bookings
                    val totalPaid = bookings
                        .filter { it.status.uppercase() in listOf("ACTIVE", "COMPLETED") }
                        .sumOf { it.amountPaid ?: BigDecimal.ZERO }
                    val cancelledBookings = bookings.filter { it.status.uppercase() == "CANCELLED" }
                    val cancelledRefundAmount = cancelledBookings.sumOf { it.amountPaid ?: BigDecimal.ZERO }

                    // Summary Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Total Amount Paid", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                            Text("?${totalPaid.toInt()}", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                Column {
                                    Text("Total Bookings", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                                    Text("${bookings.size}", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Refundable", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                                    Text("?${cancelledRefundAmount.toInt()}", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Cancelled Bookings = Refund History
                    if (cancelledBookings.isNotEmpty()) {
                        SettingsGroup(title = "Refund / Cancelled Bookings") {
                            cancelledBookings.forEach { booking ->
                                RefundStatusItem(
                                    title = booking.propertyName,
                                    amount = "?${booking.amountPaid?.toInt() ?: 0}",
                                    status = "Cancelled",
                                    date = "Booked on ${booking.createdAt.take(10)}"
                                )
                            }
                        }
                    }

                    // All Bookings Payment History
                    if (bookings.isNotEmpty()) {
                        SettingsGroup(title = "Payment History") {
                            bookings.forEach { booking ->
                                RefundStatusItem(
                                    title = booking.propertyName,
                                    amount = "?${booking.amountPaid?.toInt() ?: 0}",
                                    status = booking.status.replace("_", " "),
                                    date = booking.createdAt.take(10)
                                )
                            }
                        }
                    }
                }
                is MyBookingsState.Error -> {
                    // Fallback static summary when API fails
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Total Refund Processed", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                            Text("?0", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No payment data available", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            SettingsGroup(title = "Help & Support") {
                SettingsItem(
                    icon = Icons.Rounded.AccountBalance,
                    title = "Refund Policy",
                    subtitle = "Understand how refunds work",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Rounded.ContactSupport,
                    title = "Report Payment Issue",
                    subtitle = "Transaction failed or amount debited",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Rounded.Timer,
                    title = "Refund Timeline",
                    subtitle = "Usually 5-7 business days",
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .background(WarningOrange.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Rounded.Info, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Refunds are credited back to the original payment source. In case of UPI, it might take up to 48 hours for the bank to settle.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarningOrange,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RefundStatusItem(
    title: String,
    amount: String,
    status: String,
    date: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.History, contentDescription = null, tint = PrimaryBlue)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                Text(date, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(amount, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text(
                    status,
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
