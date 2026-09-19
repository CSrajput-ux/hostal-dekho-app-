package com.company.hostaldekho.ui.screens

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
import com.company.hostaldekho.ui.components.InnerPageTopBar
import com.company.hostaldekho.ui.components.SettingsGroup
import com.company.hostaldekho.ui.components.SettingsItem
import com.company.hostaldekho.ui.theme.*

@Composable
fun SecurityScreen(onBack: () -> Unit) {
    var twoFactorEnabled by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            InnerPageTopBar(title = "Privacy & Security", onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            SettingsGroup(title = "Security") {
                SettingsItem(
                    icon = Icons.Rounded.Password,
                    title = "Change Password",
                    subtitle = "Last changed 3 months ago",
                    onClick = { /* Navigate to Change Password */ }
                )
                SettingsItem(
                    icon = Icons.Rounded.VpnKey,
                    title = "Two-Factor Authentication",
                    subtitle = "Add an extra layer of security",
                    trailing = {
                        Switch(
                            checked = twoFactorEnabled,
                            onCheckedChange = { twoFactorEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue
                            )
                        )
                    },
                    onClick = { twoFactorEnabled = !twoFactorEnabled }
                )
                SettingsItem(
                    icon = Icons.Rounded.Fingerprint,
                    title = "Biometric Login",
                    subtitle = "Use fingerprint or face ID",
                    trailing = {
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { biometricEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue
                            )
                        )
                    },
                    onClick = { biometricEnabled = !biometricEnabled }
                )
            }

            SettingsGroup(title = "Account Privacy") {
                SettingsItem(
                    icon = Icons.Rounded.Visibility,
                    title = "Profile Visibility",
                    subtitle = "Control who can see your profile",
                    onClick = { /* Visibility settings */ }
                )
                SettingsItem(
                    icon = Icons.Rounded.Devices,
                    title = "Active Sessions",
                    subtitle = "Manage your logged-in devices",
                    onClick = { /* Session management */ }
                )
            }

            SettingsGroup(title = "Data Management") {
                SettingsItem(
                    icon = Icons.Rounded.Download,
                    title = "Download My Data",
                    subtitle = "Get a copy of your information",
                    onClick = { /* Download data */ }
                )
                SettingsItem(
                    icon = Icons.Rounded.History,
                    title = "Login History",
                    onClick = { /* History */ }
                )
            }
        }
    }
}
