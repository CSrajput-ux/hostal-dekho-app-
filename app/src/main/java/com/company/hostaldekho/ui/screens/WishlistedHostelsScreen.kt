package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
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

@Composable
fun WishlistedHostelsScreen(
    onBack: () -> Unit,
    onPropertyClick: (String) -> Unit = {},
    wishlistViewModel: WishlistViewModel = hiltViewModel()
) {
    val wishlistState by wishlistViewModel.wishlistState.collectAsState()

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            InnerPageTopBar(title = "Wishlisted Hostels", onBack = onBack)
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
                    EmptyWishlist(modifier = Modifier.padding(paddingValues))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(24.dp)
                    ) {
                        items(state.items, key = { it.id }) { item ->
                            UserPropertyCard(
                                name = item.name,
                                location = item.city,
                                price = item.startingPrice?.let { "?${it.toInt()}/month" } ?: "Price on request",
                                rating = item.rating ?: 0f,
                                imageUrl = item.images.firstOrNull() ?: "",
                                isWishlisted = true,
                                onWishlistClick = { wishlistViewModel.removeFromWishlist(item.id) },
                                onClick = { onPropertyClick(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyWishlist(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.Favorite,
            contentDescription = null,
            tint = PrimaryBlue.copy(alpha = 0.1f),
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "No Favorites Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Save your favorite hostels to see them here later and compare them easily.",
            modifier = Modifier.padding(horizontal = 48.dp),
            textAlign = TextAlign.Center,
            color = TextSecondary,
            lineHeight = 20.sp
        )
    }
}
