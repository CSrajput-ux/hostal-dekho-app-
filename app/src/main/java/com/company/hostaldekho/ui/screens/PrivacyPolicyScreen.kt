package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.hostaldekho.ui.components.InnerPageTopBar
import com.company.hostaldekho.ui.theme.BackgroundColor
import com.company.hostaldekho.ui.theme.TextPrimary
import com.company.hostaldekho.ui.theme.TextSecondary

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            InnerPageTopBar(title = "Privacy Policy", onBack = onBack)
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
                "Your Privacy Matters",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Last updated: October 2024",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(32.dp))

            PolicySection(
                title = "1. Data Collection",
                content = "We collect information you provide directly to us, such as when you create or modify your account, request on-demand services, contact customer support, or otherwise communicate with us."
            )
            PolicySection(
                title = "2. Use of Information",
                content = "The primary purpose in collecting personal information is to provide you with a safe, smooth, efficient, and customized experience. We may use your personal information to provide services and customer support you request."
            )
            PolicySection(
                title = "3. Information Sharing",
                content = "We do not sell, rent or lease your personal information to third parties. We only share your data with service providers who assist in our business operations and with property owners to facilitate your stay."
            )
            PolicySection(
                title = "4. Security",
                content = "We use industry-standard encryption and security measures to protect your data. However, no method of transmission over the Internet is 100% secure, and we cannot guarantee absolute security."
            )
        }
    }
}

@Composable
private fun PolicySection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(content, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, lineHeight = 20.sp)
    }
}
