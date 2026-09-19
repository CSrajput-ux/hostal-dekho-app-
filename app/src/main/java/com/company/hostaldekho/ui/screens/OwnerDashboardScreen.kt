package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.clickable
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
import com.company.hostaldekho.ui.components.*
import com.company.hostaldekho.ui.theme.*
import com.company.hostaldekho.ui.viewmodels.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    onBack: () -> Unit = {},
    onAddPropertyClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    propertyViewModel: PropertyViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val statsState by propertyViewModel.ownerStatsState.collectAsState()
    val propertiesState by propertyViewModel.ownerPropertiesState.collectAsState()
    val bookingsState by propertyViewModel.ownerBookingsState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        propertyViewModel.loadOwnerStats()
        propertyViewModel.loadOwnerProperties()
        propertyViewModel.loadOwnerBookings()
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("dashboard") }

    when (currentScreen) {
        "notifications" -> NotificationScreen(onBack = { currentScreen = "dashboard" })
        "settings" -> OwnerSettingsScreen(onBack = { currentScreen = "dashboard" })
        else -> {
            Scaffold(
                containerColor = BackgroundColor,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showBottomSheet = true },
                        containerColor = PrimaryBlue,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.size(64.dp)
                    ) {
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {
                    if (showBottomSheet) {
                        AddPropertyBottomSheet(
                            onDismiss = { showBottomSheet = false },
                            onOptionClick = {
                                showBottomSheet = false
                                onAddPropertyClick()
                            }
                        )
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                    item {
                        PremiumHeader(
                            ownerName = currentUser?.name ?: currentUser?.username ?: "Owner",
                            onNotificationClick = onNotificationClick,
                            onSettingsClick = onSettingsClick
                        )
                    }

                    // Stats Section
                    item {
                        Text(
                            "Overview",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val stats = (statsState as? OwnerStatsState.Success)?.stats
                        
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                StatCard(
                                    label = "Earnings",
                                    value = stats?.totalEarnings?.let { "₹$it" } ?: "₹0",
                                    growth = "Total lifetime",
                                    icon = Icons.Rounded.AccountBalanceWallet,
                                    gradient = GradientBlue,
                                    modifier = Modifier.width(180.dp)
                                )
                            }
                            item {
                                StatCard(
                                    label = "Bookings",
                                    value = stats?.totalBookings?.toString() ?: "0",
                                    growth = "${stats?.activeBookings ?: 0} Active",
                                    icon = Icons.Rounded.ConfirmationNumber,
                                    gradient = GradientGreen,
                                    modifier = Modifier.width(180.dp)
                                )
                            }
                            item {
                                StatCard(
                                    label = "Properties",
                                    value = stats?.totalProperties?.toString() ?: "0",
                                    growth = "${stats?.activeProperties ?: 0} Active",
                                    icon = Icons.Rounded.HomeWork,
                                    gradient = GradientOrange,
                                    modifier = Modifier.width(180.dp)
                                )
                            }
                        }
                    }

                    // Analytics Section
                    item {
                        Text(
                            "Analytics",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OccupancyRing(modifier = Modifier.weight(1f))
                            // We can add another ring or stats here
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                StatCard("Views", "-", "-", Icons.Rounded.Visibility, GradientPurple, Modifier.fillMaxWidth().height(112.dp))
                                StatCard("Weekly", "-", "-", Icons.Rounded.Timeline, GradientBlue, Modifier.fillMaxWidth().height(112.dp))
                            }
                        }
                        RevenueChart(modifier = Modifier.padding(horizontal = 24.dp))
                    }

                    // Quick Actions
                    item {
                        Text(
                            "Quick Actions",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            QuickActionBtn("Add Property", Icons.Rounded.AddHome, onClick = onAddPropertyClick, modifier = Modifier.weight(1f))
                            QuickActionBtn("Bookings", Icons.Rounded.EventAvailable, onClick = {}, modifier = Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            QuickActionBtn("Analytics", Icons.Rounded.BarChart, onClick = {}, modifier = Modifier.weight(1f))
                            QuickActionBtn("Payments", Icons.Rounded.Payments, onClick = {}, modifier = Modifier.weight(1f))
                        }
                    }

                    // Filter Chips
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(listOf("All", "PG", "Hostel", "Flat", "Active")) { filter ->
                                FilterChip(
                                    selected = filter == "All",
                                    onClick = {},
                                    label = { Text(filter) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Listings
                    item {
                        Text(
                            "My Listings",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    when (val pState = propertiesState) {
                        is OwnerPropertiesState.Loading -> {
                            item { CircularProgressIndicator(modifier = Modifier.padding(24.dp)) }
                        }
                        is OwnerPropertiesState.Success -> {
                            if (pState.data.properties.isEmpty()) {
                                item { Text("No properties listed", modifier = Modifier.padding(24.dp)) }
                            } else {
                                items(pState.data.properties) { property ->
                                    PropertyCard(
                                        name = property.name,
                                        location = "${property.city}",
                                        price = property.startingPrice?.let { "₹$it" } ?: "N/A",
                                        occupancy = "${((property.totalRooms - property.availableBeds).toFloat() / property.totalRooms * 100).toInt()}%",
                                        status = if (property.isActive) "Active" else "Inactive",
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                }
                            }
                        }
                        is OwnerPropertiesState.Error -> {
                            item { Text(pState.message, color = Color.Red, modifier = Modifier.padding(24.dp)) }
                        }
                    }

                    // Recent Bookings
                    item {
                        Text(
                            "Recent Bookings",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    when (val bState = bookingsState) {
                        is OwnerBookingsState.Loading -> {
                            item { CircularProgressIndicator(modifier = Modifier.padding(24.dp)) }
                        }
                        is OwnerBookingsState.Success -> {
                            val recent = bState.data.bookings.take(5)
                            if (recent.isEmpty()) {
                                item { Text("No recent bookings", modifier = Modifier.padding(24.dp)) }
                            } else {
                                items(recent) { booking ->
                                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
                                        OwnerBookingCard(
                                            Booking(
                                                id = booking.id,
                                                customerName = booking.customerName ?: "Unknown",
                                                roomType = booking.roomType,
                                                checkInDate = booking.checkInDate ?: "",
                                                duration = "${booking.durationMonths ?: 1} mo",
                                                amount = "₹${booking.amountPaid ?: 0}",
                                                status = when(booking.status.uppercase()) {
                                                    "ACTIVE" -> BookingStatus.CONFIRMED
                                                    "COMPLETED" -> BookingStatus.COMPLETED
                                                    "CANCELLED" -> BookingStatus.CANCELLED
                                                    else -> BookingStatus.PENDING
                                                }
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        is OwnerBookingsState.Error -> {
                            item { Text(bState.message, color = Color.Red, modifier = Modifier.padding(24.dp)) }
                        }
                    }

                    // Reviews
                    item {
                        Text(
                            "Latest Reviews",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        Text("No reviews yet", modifier = Modifier.padding(24.dp), color = Color.Gray)
                    }

                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyBottomSheet(
    onDismiss: () -> Unit,
    onOptionClick: () -> Unit = onDismiss
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "What would you like to add?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            val options = listOf(
                "Add PG" to Icons.Rounded.HomeWork,
                "Add Hostel" to Icons.Rounded.Business,
                "Add Flat" to Icons.Rounded.Apartment,
                "Import Listing" to Icons.Rounded.CloudUpload
            )
            
            options.forEach { (label, icon) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOptionClick() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(label, style = MaterialTheme.typography.bodyLarge)
                }
                Divider(color = Color(0xFFF1F5F9))
            }
        }
    }
}
