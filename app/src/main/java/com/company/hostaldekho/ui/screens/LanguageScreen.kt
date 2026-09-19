package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.company.hostaldekho.ui.components.InnerPageTopBar
import com.company.hostaldekho.ui.theme.*

@Composable
fun LanguageScreen(onBack: () -> Unit) {
    val languages = listOf(
        Language("English", "en", "United States"),
        Language("हिन्दी", "hi", "India"),
        Language("Español", "es", "Spain"),
        Language("Français", "fr", "France"),
        Language("Deutsch", "de", "Germany"),
        Language("日本語", "ja", "Japan")
    )
    
    var selectedLanguage by remember { mutableStateOf(languages[0]) }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            InnerPageTopBar(title = "Select Language", onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            languages.forEach { language ->
                LanguageItem(
                    language = language,
                    isSelected = selectedLanguage == language,
                    onClick = { selectedLanguage = language }
                )
            }
        }
    }
}

@Composable
private fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color.White else Color.Transparent,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) PrimaryBlue else TextPrimary
                )
                Text(
                    text = language.region,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private data class Language(val name: String, val code: String, val region: String)
