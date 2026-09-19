package com.company.hostaldekho.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.company.hostaldekho.ui.screens.*
import com.company.hostaldekho.ui.viewmodels.AuthViewModel

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * BUG-36 to BUG-47 FIXES:
 * Registered ALL 15 previously missing screens and fixed route spelling mismatches
 * (`help_center` vs `help_centre`, `refunds` vs `payment_refund`) so no user action
 * or menu button click crashes the NavController.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val handleLogout = {
        authViewModel.logout()
        navController.navigate("login") {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                onHostelClick = { hostel -> navController.navigate("detail/${hostel.id}") },
                onOwnerHubClick = { navController.navigate("owner_hub") },
                onHelpCentreClick = { navController.navigate("help_centre") },
                onLoginClick = { navController.navigate("login") },
                onSearchClick = { navController.navigate("search") }
            )
        }
        composable("search") {
            SearchScreen(onHostelClick = { hostel ->
                navController.navigate("detail/${hostel.id}")
            })
        }
        composable("detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id").orEmpty()
            DetailScreen(
                propertyId = id,
                onBack = { navController.popBackStack() },
                onBookNow = { name, price, roomId ->
                    val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
                    val encodedPrice = URLEncoder.encode(price, StandardCharsets.UTF_8.toString())
                    navController.navigate("property_booking/$id/$roomId/$encodedName/$encodedPrice")
                }
            )
        }
        composable("property_booking/{propertyId}/{roomId}/{name}/{price}") { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId").orEmpty()
            val roomId = backStackEntry.arguments?.getString("roomId").orEmpty()
            val name = backStackEntry.arguments?.getString("name").orEmpty()
            val price = backStackEntry.arguments?.getString("price").orEmpty()
            PropertyBookingScreen(
                propertyId = propertyId,
                roomId = roomId,
                hostelName = name,
                price = price,
                onBack = { navController.popBackStack() },
                onConfirm = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onOtpSent = { phone -> navController.navigate("otp/$phone") },
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate("login") { popUpTo("login") { inclusive = true } } },
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
        composable("otp/{phone}") { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone").orEmpty()
            OtpScreen(
                phone = phone,
                onBack = { navController.popBackStack() },
                onVerify = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("owner_hub") {
            OwnerMainScreen(
                onBack = { navController.popBackStack() },
                onAddPropertyClick = { navController.navigate("add_property") }
            )
        }
        composable("add_property") {
            AddPropertyScreen(onBack = { navController.popBackStack() })
        }
        composable("bank_details") {
            BankDetailsScreen(onBack = { navController.popBackStack() })
        }
        composable("owner_bank_details") {
            BankDetailsScreen(onBack = { navController.popBackStack() })
        }
        composable("payment_refund") {
            PaymentRefundScreen(onBack = { navController.popBackStack() })
        }
        composable("refunds") {
            PaymentRefundScreen(onBack = { navController.popBackStack() })
        }
        composable("help_centre") {
            HelpCentreScreen(
                onBack = { navController.popBackStack() },
                onRegisterComplaintClick = { navController.navigate("register_complaint") }
            )
        }
        composable("help_center") {
            HelpCentreScreen(
                onBack = { navController.popBackStack() },
                onRegisterComplaintClick = { navController.navigate("register_complaint") }
            )
        }
        composable("owner_help_center") {
            HelpCentreScreen(
                onBack = { navController.popBackStack() },
                onRegisterComplaintClick = { navController.navigate("register_complaint") }
            )
        }
        composable("register_complaint") {
            ComplaintScreen(onBack = { navController.popBackStack() })
        }

        // BUG-36 to BUG-47: All missing screens registered below
        composable("notifications") {
            NotificationScreen(onBack = { navController.popBackStack() })
        }
        composable("personal_info") {
            PersonalInformationScreen(onBack = { navController.popBackStack() })
        }
        composable("privacy_policy") {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }
        composable("wishlist") {
            WishlistedHostelsScreen(onBack = { navController.popBackStack() })
        }
        composable("shared_hostels") {
            SharedHostelsScreen(onBack = { navController.popBackStack() })
        }
        composable("followed_properties") {
            FollowedPropertiesScreen(onBack = { navController.popBackStack() })
        }
        composable("security") {
            SecurityScreen(onBack = { navController.popBackStack() })
        }
        composable("language") {
            LanguageScreen(onBack = { navController.popBackStack() })
        }
        composable("about") {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable("business_info") {
            BusinessInformationScreen(onBack = { navController.popBackStack() })
        }
        composable("kyc_verification") {
            KycVerificationScreen(onBack = { navController.popBackStack() })
        }
        composable("my_listings") {
            MyListingsScreen()
        }
        composable("owner_bookings") {
            OwnerBookingsScreen()
        }
        composable("owner_settings") {
            OwnerSettingsScreen(
                onBack = { navController.popBackStack() },
                onLogoutClick = handleLogout
            )
        }
        composable("paytm") {
            PaytmScreen(onBack = { navController.popBackStack() })
        }
    }
}
