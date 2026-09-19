package com.company.hostaldekho.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.R
import com.company.hostaldekho.ui.components.AppBackground
import com.company.hostaldekho.ui.theme.DangerRed
import com.company.hostaldekho.ui.theme.PrimaryBlue
import com.company.hostaldekho.ui.viewmodels.AuthState
import com.company.hostaldekho.ui.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    phone: String = "",
    onBack: () -> Unit = {},
    onVerify: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var otpValue by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                errorMessage = null
                onVerify()
                viewModel.resetState()
            }
            is AuthState.Error -> {
                errorMessage = state.message
            }
            else -> {}
        }
    }

    AppBackground(drawableRes = R.drawable.b1, overlayAlpha = 0.85f) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Verify Phone",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Enter the 6-digit code sent to +91 $phone via Firebase SMS",
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = otpValue,
                onValueChange = {
                    if (it.length <= 6) {
                        otpValue = it
                        errorMessage = null
                    }
                },
                modifier = Modifier.width(240.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    letterSpacing = 6.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                isError = errorMessage != null,
                singleLine = true
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = DangerRed,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (otpValue.length == 6) {
                        viewModel.verifyOtp(code = otpValue)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                enabled = otpValue.length == 6 && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Verify & Continue", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    otpValue = ""
                    errorMessage = null
                    if (activity != null) {
                        viewModel.sendOtp(phone, activity)
                    }
                }
            ) {
                Text("Resend Firebase SMS Code", color = PrimaryBlue)
            }
        }
    }
}
}
