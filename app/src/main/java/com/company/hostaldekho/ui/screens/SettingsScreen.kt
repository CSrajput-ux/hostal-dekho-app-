package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.company.hostaldekho.ui.components.SettingsGroup
import com.company.hostaldekho.ui.components.SettingsItem
import com.company.hostaldekho.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onPersonalInfoClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onHelpCenterClick: () -> Unit,
    onAboutClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onLogoutClick: () -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Account Section
            SettingsGroup(title = "Account") {
                SettingsItem(
                    icon = Icons.Rounded.Person,
                    title = "Personal Information",
                    subtitle = "Manage your name, email and phone",
                    onClick = onPersonalInfoClick
                )
                SettingsItem(
                    icon = Icons.Rounded.Notifications,
                    title = "Notifications",
                    subtitle = "System and marketing alerts",
                    trailing = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue
                            )
                        )
                    },
                    onClick = { notificationsEnabled = !notificationsEnabled }
                )
                SettingsItem(
                    icon = Icons.Rounded.Security,
                    title = "Privacy & Security",
                    subtitle = "Password and account security",
                    onClick = onSecurityClick
                )
            }

            // Preferences Section
            SettingsGroup(title = "Preferences") {
                SettingsItem(
                    icon = Icons.Rounded.Language,
                    title = "Language",
                    subtitle = "English (US)",
                    onClick = onLanguageClick
                )
                SettingsItem(
                    icon = Icons.Rounded.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Change app appearance",
                    trailing = {
                        Switch(
                            checked = darkModeEnabled,
                            onCheckedChange = { darkModeEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue
                            )
                        )
                    },
                    onClick = { darkModeEnabled = !darkModeEnabled }
                )
            }

            // Support Section
            SettingsGroup(title = "Support & About") {
                SettingsItem(
                    icon = Icons.Rounded.HelpCenter,
                    title = "Help Center",
                    onClick = onHelpCenterClick
                )
                SettingsItem(
                    icon = Icons.Rounded.Info,
                    title = "About HostelDekho",
                    subtitle = "Version 1.0.4",
                    onClick = onAboutClick
                )
                SettingsItem(
                    icon = Icons.Rounded.Policy,
                    title = "Terms & Privacy Policy",
                    onClick = onPrivacyPolicyClick
                )
            }

            // Danger Zone
            SettingsGroup(title = "Actions") {
                SettingsItem(
                    icon = Icons.Rounded.Logout,
                    title = "Logout",
                    iconColor = DangerRed,
                    onClick = onLogoutClick
                )
                SettingsItem(
                    icon = Icons.Rounded.DeleteForever,
                    title = "Delete Account",
                    subtitle = "Permanently remove your data",
                    iconColor = DangerRed,
                    onClick = { /* Handle Delete */ }
                )
            }
        }
    }
}
