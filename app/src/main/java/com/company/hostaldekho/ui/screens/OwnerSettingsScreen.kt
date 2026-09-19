package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.ui.components.*
import com.company.hostaldekho.ui.theme.*
import com.company.hostaldekho.ui.viewmodels.AuthViewModel

@Composable
fun OwnerSettingsScreen(
    onBack: () -> Unit = {},
    onBusinessInfoClick: () -> Unit = {},
    onBankDetailsClick: () -> Unit = {},
    onKycClick: () -> Unit = {},
    onHelpCenterClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            InnerPageTopBar(title = "Settings", onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Profile Info Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.name ?: currentUser?.username ?: "Owner",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = currentUser?.email ?: "Partner Account",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BackgroundColor)
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit Profile", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    }
                }
            }

            SettingsGroup(title = "Account Settings") {
                SettingsItem(
                    icon = Icons.Rounded.Business,
                    title = "Business Information",
                    onClick = onBusinessInfoClick
                )
                SettingsItem(
                    icon = Icons.Rounded.AccountBalance,
                    title = "Bank Details",
                    onClick = onBankDetailsClick
                )
                SettingsItem(
                    icon = Icons.Rounded.VerifiedUser,
                    title = "KYC Verification",
                    onClick = onKycClick
                )
            }

            SettingsGroup(title = "Preferences") {
                var notificationsEnabled by remember { mutableStateOf(true) }
                SettingsItem(
                    icon = Icons.Rounded.Notifications,
                    title = "Push Notifications",
                    trailing = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE2E8F0)
                            )
                        )
                    }
                )
                SettingsItem(
                    icon = Icons.Rounded.Language,
                    title = "Language",
                    subtitle = "English"
                )
            }

            SettingsGroup(title = "Support & Legal") {
                SettingsItem(
                    icon = Icons.Rounded.Help,
                    title = "Help Center",
                    onClick = onHelpCenterClick
                )
                SettingsItem(
                    icon = Icons.Rounded.Policy,
                    title = "Privacy Policy",
                    onClick = onPrivacyPolicyClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2))
            ) {
                Icon(Icons.Rounded.Logout, contentDescription = null, tint = DangerRed)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Logout", color = DangerRed, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Version 1.0.4",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.labelSmall,
                color = TextPlaceholder
            )
        }
    }
}
