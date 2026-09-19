package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.hostaldekho.ui.components.InnerPageTopBar
import com.company.hostaldekho.ui.components.SaaSButton
import com.company.hostaldekho.ui.theme.BackgroundColor
import com.company.hostaldekho.ui.theme.PrimaryBlue
import com.company.hostaldekho.ui.theme.TextPrimary
import com.company.hostaldekho.ui.theme.TextSecondary

@Composable
fun HelpCentreScreen(
    onBack: () -> Unit,
    onRegisterComplaintClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            InnerPageTopBar(title = "Help Centre", onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                "How can we help you?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(24.dp))

            HelpCategoryItem(
                icon = Icons.Rounded.SupportAgent,
                title = "24/7 Customer Support",
                description = "Chat with our experts for immediate help",
                onClick = {}
            )
            HelpCategoryItem(
                icon = Icons.Rounded.Article,
                title = "FAQs",
                description = "Find answers to common questions",
                onClick = {}
            )
            HelpCategoryItem(
                icon = Icons.Rounded.BugReport,
                title = "Report an Issue",
                description = "Facing technical difficulties? Let us know",
                onClick = onRegisterComplaintClick
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text("Contact Us Directly", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ContactRow(icon = Icons.Rounded.Call, label = "Call Support", value = "+91 7579948087")
                    Divider(color = BackgroundColor)
                    ContactRow(icon = Icons.Rounded.Email, label = "Email Us", value = "support@hostaldekho.com")
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            SaaSButton(
                text = "Register a Complaint",
                onClick = onRegisterComplaintClick
            )
        }
    }
}

@Composable
private fun HelpCategoryItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryBlue)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
private fun ContactRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text(value, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}
