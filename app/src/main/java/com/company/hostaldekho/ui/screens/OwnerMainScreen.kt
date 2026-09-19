package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.company.hostaldekho.R
import com.company.hostaldekho.ui.components.AppBackground
import com.company.hostaldekho.ui.components.RevenueChart
import com.company.hostaldekho.ui.components.RevenuePoint
import com.company.hostaldekho.ui.theme.*

sealed class OwnerBottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    object Dashboard : OwnerBottomBarScreen("owner_dashboard_content", "Dashboard", Icons.Rounded.Dashboard)
    object Properties : OwnerBottomBarScreen("my_listings", "Properties", Icons.Rounded.HomeWork)
    object Bookings : OwnerBottomBarScreen("owner_bookings", "Bookings", Icons.Rounded.EventAvailable)
    object Payments : OwnerBottomBarScreen("owner_payments", "Payments", Icons.Rounded.Payments)
    object Profile : OwnerBottomBarScreen("owner_profile", "Profile", Icons.Rounded.Person)
}

@Composable
fun OwnerMainScreen(
    onBack: () -> Unit,
    onAddPropertyClick: () -> Unit
) {
    val navController = rememberNavController()
    val screens = listOf(
        OwnerBottomBarScreen.Dashboard,
        OwnerBottomBarScreen.Properties,
        OwnerBottomBarScreen.Bookings,
        OwnerBottomBarScreen.Payments,
        OwnerBottomBarScreen.Profile
    )

    AppBackground(drawableRes = R.drawable.b2, overlayAlpha = 0.88f) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier.height(80.dp)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        label = { 
                            Text(
                                screen.title, 
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ) 
                        },
                        icon = { 
                            Icon(
                                screen.icon, 
                                contentDescription = screen.title,
                                modifier = if (selected) Modifier.size(28.dp) else Modifier.size(24.dp)
                            ) 
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8),
                            indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = OwnerBottomBarScreen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(OwnerBottomBarScreen.Dashboard.route) {
                OwnerDashboardScreen(
                    onBack = onBack,
                    onAddPropertyClick = onAddPropertyClick,
                    onNotificationClick = { navController.navigate("notifications") },
                    onSettingsClick = { 
                        // Note: The BottomBar already handles Profile, but if dashboard has a settings icon
                        navController.navigate(OwnerBottomBarScreen.Profile.route) 
                    }
                )
            }
            composable("notifications") {
                NotificationScreen(onBack = { navController.popBackStack() })
            }
            composable(OwnerBottomBarScreen.Properties.route) {
                MyListingsScreen()
            }
            composable(OwnerBottomBarScreen.Bookings.route) {
                OwnerBookingsScreen()
            }
            composable(OwnerBottomBarScreen.Payments.route) {
                PaymentsScreen()
            }
            composable(OwnerBottomBarScreen.Profile.route) {
                OwnerSettingsScreen(
                    onBack = onBack,
                    onBusinessInfoClick = { navController.navigate("business_info") },
                    onBankDetailsClick = { navController.navigate("owner_bank_details") },
                    onKycClick = { navController.navigate("kyc_verification") },
                    onHelpCenterClick = { navController.navigate("owner_help_center") },
                    onPrivacyPolicyClick = { navController.navigate("privacy_policy") }
                )
            }
            composable("business_info") {
                BusinessInformationScreen(onBack = { navController.popBackStack() })
            }
            composable("owner_bank_details") {
                BankDetailsScreen(onBack = { navController.popBackStack() })
            }
            composable("kyc_verification") {
                KycVerificationScreen(onBack = { navController.popBackStack() })
            }
            composable("owner_help_center") {
                HelpCentreScreen(
                    onBack = { navController.popBackStack() },
                    onRegisterComplaintClick = { navController.navigate("register_complaint") }
                )
            }
            composable("privacy_policy") {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }
            composable("register_complaint") {
                ComplaintScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
}



@Composable
fun PaymentsScreen() {
    val transactions = emptyList<OwnerTransaction>()

    val chartPoints = emptyList<RevenuePoint>()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            PaymentsHeader()
        }

        item {
            EarningsSummary()
        }

        item {
            RevenueChartSection(points = chartPoints)
        }

        item {
            TransactionHeader()
        }

        items(transactions) { transaction ->
            TransactionItem(transaction)
        }
    }
}

@Composable
fun PaymentsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Financial Hub",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                "Track your earnings and payouts",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        
        IconButton(
            onClick = { /* Filter */ },
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White)
                .shadow(2.dp, CircleShape)
        ) {
            Icon(Icons.Rounded.FilterList, contentDescription = "Filter", tint = TextPrimary)
        }
    }
}

@Composable
fun EarningsSummary() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EarningsCard(
            label = "Total Earnings",
            amount = "₹0",
            icon = Icons.Rounded.AccountBalanceWallet,
            gradient = GradientBlue,
            modifier = Modifier.weight(1f)
        )
        EarningsCard(
            label = "Pending Payout",
            amount = "₹0",
            icon = Icons.Rounded.PendingActions,
            gradient = GradientOrange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun EarningsCard(
    label: String,
    amount: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(amount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
fun RevenueChartSection(points: List<RevenuePoint>) {
    Column(modifier = Modifier.padding(24.dp)) {
        RevenueChart(points = points)
    }
}

@Composable
fun TransactionHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Recent Transactions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            "View All",
            style = MaterialTheme.typography.labelLarge,
            color = PrimaryBlue,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { }
        )
    }
}

@Composable
fun TransactionItem(transaction: OwnerTransaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        when (transaction.status) {
                            "Success" -> SuccessGreen.copy(alpha = 0.1f)
                            "Pending" -> WarningOrange.copy(alpha = 0.1f)
                            else -> DangerRed.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (transaction.status) {
                        "Success" -> Icons.Rounded.CheckCircle
                        "Pending" -> Icons.Rounded.Schedule
                        else -> Icons.Rounded.Error
                    },
                    contentDescription = null,
                    tint = when (transaction.status) {
                        "Success" -> SuccessGreen
                        "Pending" -> WarningOrange
                        else -> DangerRed
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.customerName, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(transaction.date, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(transaction.amount, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text(
                    transaction.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (transaction.status) {
                        "Success" -> SuccessGreen
                        "Pending" -> WarningOrange
                        else -> DangerRed
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class OwnerTransaction(
    val id: String,
    val customerName: String,
    val roomType: String,
    val amount: String,
    val status: String,
    val date: String
)
