package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.company.hostaldekho.ui.theme.PrimaryBlue
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.ui.viewmodels.PropertyViewModel
import com.company.hostaldekho.ui.viewmodels.PropertyDetailState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

import com.company.hostaldekho.ui.components.SaaSButton
import com.company.hostaldekho.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    propertyId: String = "", 
    onBack: () -> Unit = {}, 
    onBookNow: (String, String, String) -> Unit = { _, _, _ -> },
    viewModel: PropertyViewModel = hiltViewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    var isWishlisted by remember { mutableStateOf(false) }

    LaunchedEffect(propertyId) {
        if (propertyId.isNotEmpty()) {
            viewModel.getPropertyDetails(propertyId)
        }
    }
    Scaffold(
        containerColor = BackgroundColor,
        bottomBar = {
            if (detailState is PropertyDetailState.Success) {
                val property = (detailState as PropertyDetailState.Success).property
                val room = property.rooms?.firstOrNull()
                val price = room?.price?.toString() ?: "TBA"
                val roomId = room?.id ?: ""
                val hasRooms = roomId.isNotEmpty() && (room?.availabilityCount ?: 0) > 0
                DetailBottomBar(
                    price = "₹$price",
                    enabled = hasRooms,
                    buttonText = if (hasRooms) "Book Now" else "Sold Out",
                    onBookNow = { 
                        if (hasRooms) {
                            onBookNow(property.name, "₹$price", roomId)
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        when (val state = detailState) {
            is PropertyDetailState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            is PropertyDetailState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(state.message, color = DangerRed)
                }
            }
            is PropertyDetailState.Success -> {
                val property = state.property
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box {
                        AsyncImage(
                            model = property.rooms?.firstOrNull()?.images?.firstOrNull() ?: "https://images.unsplash.com/photo-1555854817-40e09807a72d",
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp),
                            contentScale = ContentScale.Crop
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.background(Color.White.copy(alpha = 0.9f), androidx.compose.foundation.shape.CircleShape)
                            ) {
                                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                            }
                            IconButton(
                                onClick = { isWishlisted = !isWishlisted },
                                modifier = Modifier.background(Color.White.copy(alpha = 0.9f), androidx.compose.foundation.shape.CircleShape)
                            ) {
                                Icon(
                                    if (isWishlisted) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = "Save",
                                    tint = if (isWishlisted) DangerRed else TextPrimary
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                property.name, 
                                style = MaterialTheme.typography.headlineMedium, 
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Surface(
                                color = PrimaryBlue.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFB400), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("4.5", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                }
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${property.address}, ${property.city}", color = TextSecondary)
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Amenities", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            FacilityItem(Icons.Rounded.Wifi, "WiFi")
                            FacilityItem(Icons.Rounded.AcUnit, "AC")
                            FacilityItem(Icons.Rounded.Restaurant, "Meals")
                            FacilityItem(Icons.Rounded.LocalLaundryService, "Laundry")
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        Text("About this stay", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            property.description ?: "No description available.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FacilityItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryBlue)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
    }
}

@Composable
private fun DetailBottomBar(
    price: String,
    enabled: Boolean = true,
    buttonText: String = "Book Now",
    onBookNow: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(price, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
                Text("including all taxes", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            SaaSButton(
                text = buttonText,
                onClick = onBookNow,
                enabled = enabled,
                modifier = Modifier.width(160.dp)
            )
        }
    }
}
