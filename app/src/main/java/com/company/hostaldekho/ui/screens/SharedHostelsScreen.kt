package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.hostaldekho.ui.components.InnerPageTopBar
import com.company.hostaldekho.ui.components.UserPropertyCard
import com.company.hostaldekho.ui.theme.BackgroundColor
import com.company.hostaldekho.ui.theme.PrimaryBlue
import com.company.hostaldekho.ui.theme.TextSecondary

@Composable
fun SharedHostelsScreen(onBack: () -> Unit) {
    // Empty for now - to be fetched from backend
    val sharedHostels = emptyList<SharedHostel>()

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            InnerPageTopBar(title = "Shared Hostels", onBack = onBack)
        }
    ) { paddingValues ->
        if (sharedHostels.isEmpty()) {
            EmptySharedState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(24.dp)
            ) {
                items(sharedHostels) { hostel ->
                    UserPropertyCard(
                        name = hostel.name,
                        location = hostel.location,
                        price = hostel.price,
                        rating = hostel.rating,
                        imageUrl = hostel.imageUrl,
                        onClick = { /* Navigate */ }
                    )
                    Text(
                        text = hostel.shareNote,
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

@Composable
private fun EmptySharedState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.Share,
            contentDescription = null,
            tint = PrimaryBlue.copy(alpha = 0.1f),
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "No Shares Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Share hostels with your friends to find the perfect place together!",
            modifier = Modifier.padding(horizontal = 48.dp),
            textAlign = TextAlign.Center,
            color = TextSecondary,
            lineHeight = 20.sp
        )
    }
}

private data class SharedHostel(
    val id: String,
    val name: String,
    val location: String,
    val price: String,
    val rating: Float,
    val imageUrl: String,
    val shareNote: String
)
