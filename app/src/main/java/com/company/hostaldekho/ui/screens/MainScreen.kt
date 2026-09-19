package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.company.hostaldekho.R
import com.company.hostaldekho.ui.components.AppBackground
import com.company.hostaldekho.ui.viewmodels.AuthViewModel

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : BottomBarScreen("home", "Home", Icons.Default.Home)
    object Bookings : BottomBarScreen("bookings", "My Bookings", Icons.Default.ListAlt)
    object Profile : BottomBarScreen("profile", "Profile", Icons.Default.Person)
    object Settings : BottomBarScreen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    onHostelClick: (com.company.hostaldekho.model.Hostel) -> Unit,
    onOwnerHubClick: () -> Unit,
    onHelpCentreClick: () -> Unit,
    onLoginClick: () -> Unit,
    onSearchClick: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Bookings,
        BottomBarScreen.Profile,
        BottomBarScreen.Settings
    )

    val handleLogout = {
        authViewModel.logout()
        onLoginClick() // Redirect to Login screen
    }

    AppBackground(drawableRes = R.drawable.b2, overlayAlpha = 0.88f) {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    NavigationBarItem(
                        label = { Text(screen.title) },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomBarScreen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomBarScreen.Home.route) {
                LandingScreen(
                    onHostelClick = onHostelClick,
                    onSearchClick = onSearchClick,
                    onNotificationClick = { navController.navigate("notifications") }
                )
            }
            composable("notifications") {
                NotificationScreen(onBack = { navController.popBackStack() })
            }
            composable(BottomBarScreen.Bookings.route) {
                BookingsScreen()
            }
            composable(BottomBarScreen.Profile.route) {
                ProfileScreen(
                    onOwnerHubClick = onOwnerHubClick,
                    onBankDetailsClick = { navController.navigate("bank_details") },
                    onPaymentsClick = { navController.navigate("refunds") },
                    onHelpCentreClick = onHelpCentreClick,
                    onLoginClick = onLoginClick,
                    onEditProfileClick = { navController.navigate("personal_info") },
                    onPrivacyPolicyClick = { navController.navigate("privacy_policy") },
                    onWishlistClick = { navController.navigate("wishlist") },
                    onSharedHostelsClick = { navController.navigate("shared_hostels") },
                    onFollowedPropertiesClick = { navController.navigate("followed_properties") },
                    onLogoutSuccess = handleLogout
                )
            }
            composable(BottomBarScreen.Settings.route) {
                SettingsScreen(
                    onPersonalInfoClick = { navController.navigate("personal_info") },
                    onSecurityClick = { navController.navigate("security") },
                    onLanguageClick = { navController.navigate("language") },
                    onHelpCenterClick = { navController.navigate("help_center") },
                    onAboutClick = { navController.navigate("about") },
                    onPrivacyPolicyClick = { navController.navigate("privacy_policy") },
                    onLogoutClick = handleLogout
                )
            }
            composable("personal_info") {
                PersonalInformationScreen(onBack = { navController.popBackStack() })
            }
            composable("security") {
                SecurityScreen(onBack = { navController.popBackStack() })
            }
            composable("language") {
                LanguageScreen(onBack = { navController.popBackStack() })
            }
            composable("help_center") {
                HelpCentreScreen(onBack = { navController.popBackStack() })
            }
            composable("about") {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            composable("privacy_policy") {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }
            composable("wishlist") {
                WishlistedHostelsScreen(onBack = { navController.popBackStack() })
            }
            composable("bank_details") {
                BankDetailsScreen(onBack = { navController.popBackStack() })
            }
            composable("refunds") {
                PaymentRefundScreen(onBack = { navController.popBackStack() })
            }
            composable("shared_hostels") {
                SharedHostelsScreen(onBack = { navController.popBackStack() })
            }
            composable("followed_properties") {
                FollowedPropertiesScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
}
