package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.hostaldekho.ui.components.InnerPageTopBar
import com.company.hostaldekho.ui.theme.*

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            InnerPageTopBar(title = "About", onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Logo Placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.HomeWork,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "HostelDekho",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                "Version 1.0.4 (Stable)",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                "Our Mission",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "HostelDekho is dedicated to simplifying the hostel search and management process. We connect students and working professionals with the best accommodations while providing owners with powerful tools to manage their properties seamlessly.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 22.sp,
                textAlign = TextAlign.Start
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                "Connect With Us",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SocialIcon(Icons.Rounded.Language)
                SocialIcon(Icons.Rounded.Public) // For Instagram/Web
                SocialIcon(Icons.Rounded.Chat) // For WhatsApp
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                "© 2024 HostelDekho Inc. All rights reserved.",
                style = MaterialTheme.typography.labelSmall,
                color = TextPlaceholder
            )
            Text(
                "Made with ♥ in India",
                style = MaterialTheme.typography.labelSmall,
                color = TextPlaceholder
            )
        }
    }
}

@Composable
private fun SocialIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.White)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryBlue)
    }
}
