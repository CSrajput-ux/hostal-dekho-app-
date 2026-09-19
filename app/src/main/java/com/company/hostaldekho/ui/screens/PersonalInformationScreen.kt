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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.ui.components.InnerPageTopBar
import com.company.hostaldekho.ui.components.SaaSButton
import com.company.hostaldekho.ui.theme.*
import com.company.hostaldekho.ui.viewmodels.AuthState
import com.company.hostaldekho.ui.viewmodels.AuthViewModel

@Composable
fun PersonalInformationScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val updateState by authViewModel.profileUpdateState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember(currentUser) { mutableStateOf(currentUser?.name ?: currentUser?.username ?: "") }
    var email by remember(currentUser) { mutableStateOf(currentUser?.email ?: "") }
    var phone by remember(currentUser) { mutableStateOf(currentUser?.mobile ?: "") }
    var gender by remember { mutableStateOf("Male") }

    LaunchedEffect(updateState) {
        val currentStatus = updateState
        when (currentStatus) {
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(currentStatus.message)
            }
            is AuthState.UpdateSuccess -> {
                snackbarHostState.showSnackbar("Profile updated successfully")
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            InnerPageTopBar(title = "Personal Information", onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture Section
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = PrimaryBlue
                    )
                }
                IconButton(
                    onClick = { /* Change Photo */ },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue)
                ) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "Edit Photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Form Fields
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                InfoInputField(
                    label = "Username",
                    value = name,
                    onValueChange = { name = it },
                    icon = Icons.Rounded.AlternateEmail
                )
                InfoInputField(
                    label = "Email Address",
                    value = email,
                    onValueChange = { email = it },
                    icon = Icons.Rounded.Email
                )
                InfoInputField(
                    label = "Phone Number",
                    value = phone,
                    onValueChange = { phone = it },
                    icon = Icons.Rounded.Phone
                )
                
                // Gender Selection (Simplified)
                Text(
                    "Gender",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Male", "Female", "Other").forEach { option ->
                        val selected = gender == option
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { gender = option },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) PrimaryBlue else Color.White,
                            border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = option,
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = if (selected) Color.White else TextPrimary,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            SaaSButton(
                text = "Save Changes",
                onClick = { 
                    authViewModel.updateProfile(name, email, phone, gender)
                },
                isLoading = updateState is AuthState.Loading
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary,
            modifier = Modifier.padding(start = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(icon, contentDescription = null, tint = PrimaryBlue) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}
