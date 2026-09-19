package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.ui.components.InnerPageTopBar
import com.company.hostaldekho.ui.theme.*
import com.company.hostaldekho.ui.viewmodels.MyBookingsState
import com.company.hostaldekho.ui.viewmodels.PropertyViewModel

// Derives notification-style items from real bookings data
@Composable
fun NotificationScreen(
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
            InnerPageTopBar(title = "Notifications", onBack = onBack)
        }
    ) { paddingValues ->
        when (val state = myBookingsState) {
            is MyBookingsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is MyBookingsState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Could not load notifications", color = TextSecondary)
                }
            }
            is MyBookingsState.Success -> {
                val bookings = state.data.bookings
                if (bookings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.Notifications,
                                contentDescription = null,
                                tint = PrimaryBlue.copy(alpha = 0.12f),
                                modifier = Modifier.size(96.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No new notifications",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(bookings, key = { it.id }) { booking ->
                            // Map booking status to notification style
                            val (icon, color, message) = when (booking.status.uppercase()) {
                                "ACTIVE" -> Triple(
                                    Icons.Rounded.EventAvailable,
                                    SuccessGreen,
                                    "Your booking at ${booking.propertyName} is active."
                                )
                                "PENDING_PAYMENT" -> Triple(
                                    Icons.Rounded.Payments,
                                    WarningOrange,
                                    "Payment pending for ${booking.propertyName}."
                                )
                                "COMPLETED" -> Triple(
                                    Icons.Rounded.Receipt,
                                    PrimaryBlue,
                                    "Your stay at ${booking.propertyName} is completed."
                                )
                                "CANCELLED" -> Triple(
                                    Icons.Rounded.Receipt,
                                    DangerRed,
                                    "Booking at ${booking.propertyName} was cancelled."
                                )
                                else -> Triple(
                                    Icons.Rounded.Notifications,
                                    TextSecondary,
                                    "Update for booking at ${booking.propertyName}."
                                )
                            }

                            NotificationCard(
                                title = "Booking ${booking.status.replace("_", " ")}",
                                message = message,
                                time = booking.createdAt.take(10),
                                icon = icon,
                                color = color
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    title: String,
    message: String,
    time: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(time, fontSize = 10.sp, color = TextPlaceholder, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val icon: ImageVector,
    val color: Color
)
