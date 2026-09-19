package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.company.hostaldekho.model.Hostel
import com.company.hostaldekho.ui.components.UserPropertyCard
import com.company.hostaldekho.ui.theme.TextPrimary
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.ui.viewmodels.PropertyViewModel
import com.company.hostaldekho.ui.viewmodels.PropertyListState

/**
 * BUG-33 FIX: Added search input TextField so users can search properties by city or name.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onHostelClick: (Hostel) -> Unit = {},
    viewModel: PropertyViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.searchProperties()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Properties", color = TextPrimary) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // BUG-33 FIX: Search TextField
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchProperties(if (it.isBlank()) null else it)
                },
                placeholder = { Text("Search by city, location or hostel name...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = listState) {
                    is PropertyListState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is PropertyListState.Error -> {
                        item {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }
                    is PropertyListState.Success -> {
                        val searchResults = state.properties
                        item {
                            Text(
                                text = "${searchResults.size} properties found",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        if (searchResults.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No properties match your search", color = Color.Gray)
                                }
                            }
                        } else {
                            items(searchResults) { property ->
                                val priceString = if (property.startingPrice != null) "₹${property.startingPrice}/mo" else "Price on request"
                                val fallbackImage = "https://images.unsplash.com/photo-1555854817-40e09807a72d"

                                UserPropertyCard(
                                    name = property.name,
                                    location = "${property.address}, ${property.city}",
                                    price = priceString,
                                    rating = 4.5f,
                                    imageUrl = fallbackImage,
                                    onClick = {
                                        onHostelClick(
                                            Hostel(
                                                id = property.id,
                                                name = property.name,
                                                price = priceString,
                                                rating = 4.5f,
                                                location = property.city,
                                                imageUrl = fallbackImage,
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
    }
}
