package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.ui.viewmodels.AuthViewModel
import com.company.hostaldekho.ui.viewmodels.AuthState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.hostaldekho.ui.components.*
import com.company.hostaldekho.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOwnerHubClick: () -> Unit = {},
    onBankDetailsClick: () -> Unit = {},
    onPaymentsClick: () -> Unit = {},
    onHelpCentreClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onWishlistClick: () -> Unit = {},
    onSharedHostelsClick: () -> Unit = {},
    onFollowedPropertiesClick: () -> Unit = {},
    onLogoutSuccess: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel()
) {
    // FIX: Use real auth state from AuthViewModel instead of hardcoded false
    val isLoggedIn = authViewModel.isLoggedIn()
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Extract user info from successful auth state or currentUser flow
    val displayName = currentUser?.name ?: currentUser?.username ?: when (val s = authState) {
        is AuthState.Success -> s.response.user.name ?: s.response.user.username ?: "User"
        else -> "HostelDekho User"
    }
    val displayMobile = currentUser?.mobile?.let { "+91 $it" } ?: when (val s = authState) {
        is AuthState.Success -> s.response.user.mobile?.let { "+91 $it" } ?: ""
        else -> ""
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Info Header or Sign In Button
            if (isLoggedIn) {
                ProfileHeader(
                    name = displayName,
                    phone = displayMobile.ifBlank { "Mobile not linked" },
                    onEditClick = onEditProfileClick
                )
            } else {
                SignInHeader(onLoginClick = onLoginClick)
            }

            // Owner Hub Quick Action
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .clickable { onOwnerHubClick() },
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
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryPurple.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.BusinessCenter, contentDescription = null, tint = PrimaryPurple)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Switch to Owner Hub", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Manage your listed properties", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextPlaceholder)
                }
            }

            // Share App Section (Newly Designed)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Spread the Word", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Share HostelDekho with friends and earn rewards!", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { /* Handle Share */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share Now", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                    Icon(
                        Icons.Rounded.Celebration,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            // My Payments Section
            SettingsGroup(title = "Financials") {
                SettingsItem(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    title = "Bank & UPI Details",
                    subtitle = "Manage your payout methods",
                    onClick = onBankDetailsClick
                )
                SettingsItem(
                    icon = Icons.Rounded.ReceiptLong,
                    title = "Payment History",
                    subtitle = "Track all your transactions",
                    onClick = onPaymentsClick
                )
            }

            // Activity Section
            SettingsGroup(title = "My Activity") {
                SettingsItem(
                    icon = Icons.Rounded.Favorite,
                    title = "Wishlisted Hostels",
                    onClick = onWishlistClick
                )
                SettingsItem(
                    icon = Icons.Rounded.Share,
                    title = "Shared Hostels",
                    onClick = onSharedHostelsClick
                )
                SettingsItem(
                    icon = Icons.Rounded.Storefront,
                    title = "Followed Properties",
                    onClick = onFollowedPropertiesClick
                )
            }

            // Support & Legal
            SettingsGroup(title = "Support") {
                SettingsItem(
                    icon = Icons.Rounded.HelpCenter,
                    title = "Help Centre",
                    onClick = onHelpCentreClick
                )
                SettingsItem(
                    icon = Icons.Rounded.Policy,
                    title = "Legal and Policies",
                    onClick = onPrivacyPolicyClick
                )
                SettingsItem(
                    icon = Icons.Rounded.Star,
                    title = "Rate HostelDekho",
                    onClick = { /* Open Play Store */ }
                )
            }

            if (isLoggedIn) {
                SettingsGroup(title = "Account Actions") {
                    SettingsItem(
                        icon = Icons.Rounded.Logout,
                        title = "Logout",
                        iconColor = DangerRed,
                        onClick = {
                            authViewModel.logout()
                            onLogoutSuccess()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Made with ❤️ for Students",
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = TextPlaceholder,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SignInHeader(onLoginClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text("Welcome to HostelDekho", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text("Sign in to sync your bookings", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        
        Button(
            onClick = onLoginClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Sign In", fontWeight = FontWeight.Bold)
        }
    }
}
