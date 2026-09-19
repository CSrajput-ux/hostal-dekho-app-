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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.ui.components.InnerPageTopBar
import com.company.hostaldekho.ui.components.SaaSButton
import com.company.hostaldekho.ui.theme.*
import com.company.hostaldekho.ui.viewmodels.KycState
import com.company.hostaldekho.ui.viewmodels.KycViewModel

@Composable
fun KycVerificationScreen(
    onBack: () -> Unit,
    kycViewModel: KycViewModel = hiltViewModel()
) {
    val kycState by kycViewModel.kycState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Known document types to show
    val docTypes = listOf(
        Triple("AADHAAR", "Aadhaar Card", Icons.Rounded.Badge),
        Triple("PAN", "PAN Card", Icons.Rounded.CreditCard),
        Triple("DRIVING_LICENSE", "Property Ownership Proof", Icons.Rounded.Description)
    )

    LaunchedEffect(kycState) {
        when (val state = kycState) {
            is KycState.UploadSuccess -> {
                snackbarHostState.showSnackbar("Document uploaded successfully!")
            }
            is KycState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                kycViewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            InnerPageTopBar(title = "KYC Verification", onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.VerifiedUser,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Trust & Safety", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "Verify your identity to unlock all platform features.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Required Documents", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = kycState) {
                is KycState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                    }
                }
                is KycState.Loaded -> {
                    // Show each known doc type with real backend status
                    docTypes.forEach { (type, title, icon) ->
                        val doc = state.documents.firstOrNull { it.documentType == type }
                        val (statusText, statusColor) = when (doc?.status) {
                            "VERIFIED" -> "Verified" to SuccessGreen
                            "UNDER_REVIEW" -> "Under Review" to WarningOrange
                            "REJECTED" -> "Rejected" to DangerRed
                            "PENDING" -> "Pending" to WarningOrange
                            else -> "Not Uploaded" to TextSecondary
                        }
                        KycDocumentItem(
                            title = title,
                            status = statusText,
                            statusColor = statusColor,
                            icon = icon,
                            onClick = if (doc == null) ({
                                // Upload placeholder doc URL (in real app: camera/file picker)
                                kycViewModel.uploadDocument(type, "https://placeholder.url/$type")
                            }) else null
                        )
                    }
                }
                is KycState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            state.message,
                            modifier = Modifier.padding(16.dp),
                            color = DangerRed,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Still show doc types as not uploaded when error
                    docTypes.forEach { (_, title, icon) ->
                        KycDocumentItem(
                            title = title,
                            status = "Not Uploaded",
                            statusColor = TextSecondary,
                            icon = icon
                        )
                    }
                }
                else -> {
                    docTypes.forEach { (_, title, icon) ->
                        KycDocumentItem(
                            title = title,
                            status = "Not Uploaded",
                            statusColor = TextSecondary,
                            icon = icon
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            SaaSButton(
                text = "Upload New Document",
                onClick = {
                    // In real app: open document picker
                    // For now, trigger a sample upload for demo
                    kycViewModel.uploadDocument("AADHAAR", "https://placeholder.url/aadhaar")
                },
                isLoading = kycState is KycState.Loading
            )
        }
    }
}

@Composable
private fun KycDocumentItem(
    title: String,
    status: String,
    statusColor: Color,
    icon: ImageVector,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
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
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(status, fontSize = 12.sp, color = statusColor, fontWeight = FontWeight.Bold)
            }
            if (onClick != null) {
                Icon(Icons.Rounded.FileUpload, contentDescription = null, tint = PrimaryBlue)
            } else {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SuccessGreen)
            }
        }
    }
}
