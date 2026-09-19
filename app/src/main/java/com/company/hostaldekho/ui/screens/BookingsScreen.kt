package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.ui.components.OwnerBookingCard
import com.company.hostaldekho.ui.theme.PrimaryBlue
import com.company.hostaldekho.ui.viewmodels.MyBookingsState
import com.company.hostaldekho.ui.viewmodels.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    viewModel: PropertyViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Booked", "Pending", "Canceled")

    val bookingsState by viewModel.myBookingsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyBookings()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "My Bookings",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                )
                Divider(thickness = 1.dp, color = Color.LightGray)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search and Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search booking...") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Box {
                    OutlinedIconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        filters.forEach { filter ->
                            DropdownMenuItem(
                                text = { Text(filter) },
                                onClick = {
                                    selectedFilter = filter
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(thickness = 1.dp, color = Color.LightGray)

            when (val state = bookingsState) {
                is MyBookingsState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is MyBookingsState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red)
                    }
                }
                is MyBookingsState.Success -> {
                    val bookings = state.data.bookings.filter {
                        (selectedFilter == "All" || it.status == selectedFilter) &&
                        (it.propertyId.contains(searchQuery, ignoreCase = true)) // Simple filter for now
                    }

                    if (bookings.isEmpty()) {
                        EmptyBookingsState(selectedFilter)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(bookings) { booking ->
                                // For now using OwnerBookingCard with some adaptation if needed or a specific card
                                // Mapping backend BookingRead to the UI model if they differ
                                Text("Booking ID: ${booking.id} - Status: ${booking.status}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyBookingsState(filter: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.EventBusy, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.Gray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No bookings found for '$filter'",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}
