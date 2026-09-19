package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.ui.components.PropertyCard
import com.company.hostaldekho.ui.theme.BackgroundColor
import com.company.hostaldekho.ui.theme.PrimaryBlue
import com.company.hostaldekho.ui.viewmodels.OwnerPropertiesState
import com.company.hostaldekho.ui.viewmodels.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingsScreen(
    viewModel: PropertyViewModel = hiltViewModel()
) {
    val propertiesState by viewModel.ownerPropertiesState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOwnerProperties()
    }
    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Properties", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Rounded.FilterList, contentDescription = "Filter")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundColor)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                placeholder = { Text("Search your properties...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = PrimaryBlue
                )
            )

            // Properties List
            when (val state = propertiesState) {
                is OwnerPropertiesState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is OwnerPropertiesState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red)
                    }
                }
                is OwnerPropertiesState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (state.data.properties.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No properties found", color = Color.Gray)
                                }
                            }
                        } else {
                            items(state.data.properties) { property ->
                                PropertyCard(
                                    name = property.name,
                                    location = property.city,
                                    price = property.startingPrice?.let { "₹$it" } ?: "N/A",
                                    occupancy = "${((property.totalRooms - property.availableBeds).toFloat() / property.totalRooms * 100).toInt()}%",
                                    status = if (property.isActive) "Active" else "Inactive"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
