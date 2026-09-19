package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.ui.components.InnerPageTopBar
import com.company.hostaldekho.ui.components.UserPropertyCard
import com.company.hostaldekho.ui.theme.BackgroundColor
import com.company.hostaldekho.ui.theme.PrimaryBlue
import com.company.hostaldekho.ui.theme.TextSecondary
import com.company.hostaldekho.ui.viewmodels.WishlistState
import com.company.hostaldekho.ui.viewmodels.WishlistViewModel

// FollowedProperties reuses the wishlist backend (same concept — saved/followed properties)
@Composable
fun FollowedPropertiesScreen(
    onBack: () -> Unit,
    onPropertyClick: (String) -> Unit = {},
    wishlistViewModel: WishlistViewModel = hiltViewModel()
) {
    val wishlistState by wishlistViewModel.wishlistState.collectAsState()

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            InnerPageTopBar(title = "Followed Properties", onBack = onBack)
        }
    ) { paddingValues ->
        when (val state = wishlistState) {
            is WishlistState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is WishlistState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                        TextButton(onClick = { wishlistViewModel.loadWishlist() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            is WishlistState.Success -> {
                if (state.items.isEmpty()) {
                    EmptyFollowedState(modifier = Modifier.padding(paddingValues))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(24.dp)
                    ) {
                        items(state.items, key = { it.id }) { property ->
                            UserPropertyCard(
                                name = property.name,
                                location = property.city,
                                price = property.startingPrice?.let { "?${it.toInt()}/month" } ?: "Price on request",
                                rating = property.rating ?: 0f,
                                imageUrl = property.images.firstOrNull() ?: "",
                                isWishlisted = true,
                                onWishlistClick = { wishlistViewModel.removeFromWishlist(property.id) },
                                onClick = { onPropertyClick(property.id) }
                            )
                            Text(
                                text = "Available in ${property.city}",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 12.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFollowedState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.Storefront,
            contentDescription = null,
            tint = PrimaryBlue.copy(alpha = 0.1f),
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "No Properties Followed",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Follow properties to get instant updates about room availability and price drops.",
            modifier = Modifier.padding(horizontal = 48.dp),
            textAlign = TextAlign.Center,
            color = TextSecondary,
            lineHeight = 20.sp
        )
    }
}
