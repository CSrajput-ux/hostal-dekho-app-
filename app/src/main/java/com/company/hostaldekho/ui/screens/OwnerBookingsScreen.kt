package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.model.Booking
import com.company.hostaldekho.model.BookingStatus
import com.company.hostaldekho.ui.components.OwnerBookingCard
import com.company.hostaldekho.ui.components.PremiumSearchBar
import com.company.hostaldekho.ui.theme.*
import com.company.hostaldekho.ui.viewmodels.OwnerBookingsState
import com.company.hostaldekho.ui.viewmodels.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerBookingsScreen(
    viewModel: PropertyViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pending", "Confirmed", "Completed", "Cancelled")

    val bookingsState by viewModel.ownerBookingsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOwnerBookings()
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(top = 16.dp)
            ) {
                Text(
                    "Manage Bookings",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                PremiumSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search by name or room type...",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedFilter == filter,
                                borderColor = if (selectedFilter == filter) Color.Transparent else Color(0xFFE2E8F0)
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) { paddingValues ->
        when (val state = bookingsState) {
            is OwnerBookingsState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            is OwnerBookingsState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = DangerRed)
                }
            }
            is OwnerBookingsState.Success -> {
                val bookings = state.data.bookings.filter {
                    (selectedFilter == "All" || it.status == selectedFilter) &&
                    (it.customerName?.contains(searchQuery, ignoreCase = true) == true || 
                     it.propertyName.contains(searchQuery, ignoreCase = true))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (bookings.isEmpty()) {
                        item {
                            EmptyBookingsState()
                        }
                    } else {
                        items(bookings) { booking ->
                            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                                // Map Backend booking to UI model
                                val uiBooking = Booking(
                                    id = booking.id,
                                    customerName = booking.customerName ?: "Unknown",
                                    roomType = booking.roomType,
                                    checkInDate = booking.checkInDate ?: booking.createdAt.split("T").first(),
                                    duration = "${booking.durationMonths ?: 1} Months",
                                    amount = "₹${booking.amountPaid ?: 0}",
                                    status = when(booking.status.uppercase()) {
                                        "PENDING", "PENDING_PAYMENT" -> BookingStatus.PENDING
                                        "ACTIVE" -> BookingStatus.CONFIRMED
                                        "COMPLETED" -> BookingStatus.COMPLETED
                                        else -> BookingStatus.CANCELLED
                                    },
                                    paymentStatus = if (booking.amountPaid != null) "Paid" else "Pending"
                                )
                                OwnerBookingCard(
                                    booking = uiBooking,
                                    onConfirm = { /* TODO */ },
                                    onDecline = { /* TODO */ },
                                    onDetails = { /* TODO */ }
                                )
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyBookingsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.EventBusy,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = TextPlaceholder.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "No bookings found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            "Try adjusting your filters or search query",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
