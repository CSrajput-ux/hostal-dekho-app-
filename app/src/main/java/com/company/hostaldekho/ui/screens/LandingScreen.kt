package com.company.hostaldekho.ui.screens

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.rounded.*
import com.company.hostaldekho.util.IndiaLocations
import com.company.hostaldekho.util.IndiaState
import com.company.hostaldekho.R
import com.company.hostaldekho.model.Hostel
import com.company.hostaldekho.ui.components.*
import com.company.hostaldekho.ui.theme.PrimaryBlue
import com.company.hostaldekho.ui.theme.PrimaryPurple
import com.company.hostaldekho.ui.theme.TextPrimary
import com.company.hostaldekho.ui.theme.TextSecondary
import com.company.hostaldekho.ui.viewmodels.PropertyListState
import com.company.hostaldekho.ui.viewmodels.PropertyViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

// BUG-28 FIX: Removed dead import `com.company.hostaldekho.data.HostelDekhoApi`

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LandingScreen(
    onHostelClick: (Hostel) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    viewModel: PropertyViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    var showLocationPicker by remember { mutableStateOf(false) }

    if (showLocationPicker) {
        IndiaLocationPickerDialog(
            onDismiss = { showLocationPicker = false },
            onLocationSelected = { selectedLocation ->
                viewModel.selectManualLocation(selectedLocation)
            }
        )
    }

    val locationPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            viewModel.startLocationUpdates()
        } else {
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("HostelDekho", fontWeight = FontWeight.Bold)
                        LocationSelector(
                            currentLocation = userLocation?.city ?: "Select Location (India)...",
                            onLocationClick = { showLocationPicker = true }
                        )
                    }
                },
                    actions = {
                        IconButton(onClick = onNotificationClick) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    ) { Text("3") }
                                }
                            ) {
                                Icon(
                                    Icons.Rounded.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item { HeroSection(onSearchClick = onSearchClick) }
            item {
                LocationFilterStrip(
                    selectedLocation = userLocation?.city ?: "All",
                    onLocationSelect = { city ->
                        viewModel.searchProperties(if (city == "All") null else city)
                    }
                )
            }
            item { CategoriesSection() }

            val title = if (userLocation?.city != null) {
                "Hostels near ${userLocation!!.city}"
            } else {
                "Featured Properties"
            }

            when (val state = listState) {
                is PropertyListState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryBlue)
                        }
                    }
                }

                is PropertyListState.Error -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(state.message, color = Color.Red)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                userLocation?.let {
                                    viewModel.fetchNearbyProperties(it.latitude, it.longitude)
                                } ?: viewModel.searchProperties()
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is PropertyListState.Success -> {
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (state.properties.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No hostels found in this area", color = Color.Gray)
                                }
                            } else {
                                state.properties.forEach { property ->
                                    UserPropertyCard(
                                        name = property.name,
                                        location = "${property.address}, ${property.city}",
                                        price = property.startingPrice?.let { "₹$it/mo" }
                                            ?: "Price on request",
                                        rating = 4.5f,
                                        imageUrl = "https://images.unsplash.com/photo-1555854817-40e09807a72d",
                                        onClick = {
                                            onHostelClick(
                                                Hostel(
                                                    id = property.id,
                                                    name = property.name,
                                                    price = property.startingPrice?.let { "₹$it/mo" }
                                                        ?: "Price on request",
                                                    rating = 4.5f,
                                                    location = property.city,
                                                    imageUrl = "https://images.unsplash.com/photo-1555854817-40e09807a72d",
                                                    category = property.type.name,
                                                    isVerified = property.verifiedBadge
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { TrustSection() }
            item { FooterSection() }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BUG-29 FIX: Removed dead getMockHostels() and FeaturedSection functions
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroSection(onSearchClick: () -> Unit) {
    val images = remember { listOf(R.drawable.b1, R.drawable.b2) }
    var currentImageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(5000L)
            currentImageIndex = (currentImageIndex + 1) % images.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        // Changing background image every 5 seconds with crossfade transition
        Crossfade(
            targetState = images[currentImageIndex],
            animationSpec = tween(durationMillis = 1000),
            label = "HeroImageTransition"
        ) { imageRes ->
            AsyncImage(
                model = imageRes,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Overlay with gradient for blue/purple branding and legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PrimaryBlue.copy(alpha = 0.65f),
                            PrimaryPurple.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Find Your Perfect Stay",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(16.dp))
            SearchBarField(onSearchClick = onSearchClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarField(onSearchClick: () -> Unit) {
    var text by remember { mutableStateOf("") }
    TextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text("Search location, price, gender...") },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp)),
        leadingIcon = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun CategoriesSection() {
    val categories = listOf("Boys Hostel", "Girls Hostel", "PG", "Flats")
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Categories", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CategoryCard(categories[0], Modifier.weight(1f))
            CategoryCard(categories[1], Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CategoryCard(categories[2], Modifier.weight(1f))
            CategoryCard(categories[3], Modifier.weight(1f))
        }
    }
}

@Composable
fun CategoryCard(name: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun LocationSelector(currentLocation: String, onLocationClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onLocationClick() }
    ) {
        Icon(
            Icons.Rounded.LocationOn,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = currentLocation,
            style = MaterialTheme.typography.labelMedium,
            color = PrimaryBlue,
            fontWeight = FontWeight.Bold
        )
        Icon(
            Icons.Rounded.ExpandMore,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(14.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationFilterStrip(selectedLocation: String, onLocationSelect: (String) -> Unit) {
    val locations = IndiaLocations.topHostelHubs

    ScrollableTabRow(
        selectedTabIndex = locations.indexOf(selectedLocation).coerceAtLeast(0),
        edgePadding = 16.dp,
        containerColor = Color.Transparent,
        divider = {},
        indicator = {},
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        locations.forEach { location ->
            val isSelected = selectedLocation == location
            FilterChip(
                selected = isSelected,
                onClick = { onLocationSelect(location) },
                label = { Text(location) },
                modifier = Modifier.padding(horizontal = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.LightGray,
                    selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IndiaLocationPickerDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedState by remember { mutableStateOf<IndiaState?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Select Location (India)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "All 28 States & 8 Union Territories",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search input for any state or district
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search any State or District in India...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = PrimaryBlue) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (searchQuery.isNotBlank()) {
                val filteredDistricts = remember(searchQuery) {
                    IndiaLocations.getAllDistrictsFormatted().filter {
                        it.contains(searchQuery, ignoreCase = true)
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            "Matching Districts & Cities (${filteredDistricts.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(filteredDistricts) { district ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLocationSelected(district)
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(district, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            "Popular Student & Hostel Hubs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IndiaLocations.topHostelHubs.forEach { hub ->
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        onLocationSelected(hub)
                                        onDismiss()
                                    },
                                    label = { Text(hub, fontWeight = FontWeight.Medium) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = PrimaryBlue.copy(alpha = 0.08f),
                                        labelColor = PrimaryBlue
                                    )
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "All States & Union Territories",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(IndiaLocations.states) { stateItem ->
                        val isExpanded = selectedState?.name == stateItem.name
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedState = if (isExpanded) null else stateItem
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isExpanded) PrimaryBlue.copy(alpha = 0.05f) else Color(0xFFF8FAFC)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            stateItem.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = TextPrimary
                                        )
                                        Text(
                                            "${stateItem.districts.size} Districts/Cities",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    Icon(
                                        if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                        contentDescription = null,
                                        tint = PrimaryBlue
                                    )
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = Color(0xFFE2E8F0))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    stateItem.districts.forEach { district ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onLocationSelected("$district, ${stateItem.name}")
                                                    onDismiss()
                                                }
                                                .padding(vertical = 10.dp, horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(district, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrustSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .background(Color(0xFFF1F3F5), RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Why Trust HostelDekho?",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Verified Properties • Secure Booking • Real Reviews",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun FooterSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("© 2024 HostelDekho Inc.", color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TextButton(onClick = {}) { Text("Privacy") }
            TextButton(onClick = {}) { Text("Terms") }
            TextButton(onClick = {}) { Text("Contact") }
        }
    }
}
