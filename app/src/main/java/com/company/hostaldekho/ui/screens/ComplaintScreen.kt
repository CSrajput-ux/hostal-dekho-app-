package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.ui.viewmodels.SupportState
import com.company.hostaldekho.ui.viewmodels.SupportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintScreen(
    onBack: () -> Unit,
    supportViewModel: SupportViewModel = hiltViewModel()
) {
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val supportState by supportViewModel.supportState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show success dialog when ticket is submitted
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(supportState) {
        when (val state = supportState) {
            is SupportState.SubmitSuccess -> {
                showSuccessDialog = true
            }
            is SupportState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                supportViewModel.resetState()
            }
            else -> {}
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                supportViewModel.resetState()
                onBack()
            },
            icon = {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    supportViewModel.resetState()
                    onBack()
                }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("Complaint Registered", fontWeight = FontWeight.Bold) },
            text = { Text("Your complaint has been successfully submitted. We will look into it shortly.") }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Register a Complaint", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Please describe your issue",
                style = MaterialTheme.typography.titleMedium,
                color = Color.DarkGray
            )

            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Wifi not working, Water issue") },
                shape = RoundedCornerShape(12.dp),
                enabled = supportState !is SupportState.Loading
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("Provide more details about your complaint...") },
                shape = RoundedCornerShape(12.dp),
                enabled = supportState !is SupportState.Loading
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    supportViewModel.submitComplaint(subject, description)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
                enabled = subject.isNotEmpty() && description.isNotEmpty() && supportState !is SupportState.Loading
            ) {
                if (supportState is SupportState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("SUBMIT", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
