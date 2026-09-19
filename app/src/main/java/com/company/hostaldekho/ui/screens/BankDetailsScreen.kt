package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.ui.components.InnerPageTopBar
import com.company.hostaldekho.ui.components.SaaSButton
import com.company.hostaldekho.ui.theme.BackgroundColor
import com.company.hostaldekho.ui.theme.PrimaryBlue
import com.company.hostaldekho.ui.viewmodels.BankState
import com.company.hostaldekho.ui.viewmodels.BankViewModel

@Composable
fun BankDetailsScreen(
    onBack: () -> Unit,
    bankViewModel: BankViewModel = hiltViewModel()
) {
    val bankState by bankViewModel.bankState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var accountHolderName by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }

    // Pre-fill from existing primary account if available
    LaunchedEffect(bankState) {
        when (val state = bankState) {
            is BankState.Loaded -> {
                val primary = state.accounts.firstOrNull { it.isPrimary } ?: state.accounts.firstOrNull()
                if (primary != null) {
                    accountHolderName = primary.accountHolderName
                    bankName = primary.bankName
                    accountNumber = primary.accountNumber
                    ifscCode = primary.ifscCode
                }
            }
            is BankState.SaveSuccess -> {
                snackbarHostState.showSnackbar("Bank details saved successfully!")
                bankViewModel.resetState()
                onBack()
            }
            is BankState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                bankViewModel.loadBankAccounts()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            InnerPageTopBar(title = "Bank Details", onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                "Payout Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "This account will be used for all your booking payouts.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Show existing accounts summary
            if (bankState is BankState.Loaded) {
                val accounts = (bankState as BankState.Loaded).accounts
                if (accounts.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "${accounts.size} account(s) saved",
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                                Text(
                                    "Primary: ${accounts.firstOrNull { it.isPrimary }?.bankName ?: accounts.first().bankName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryBlue.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = accountHolderName,
                        onValueChange = { accountHolderName = it },
                        label = { Text("Account Holder Name") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = PrimaryBlue) },
                        shape = RoundedCornerShape(16.dp),
                        enabled = bankState !is BankState.Loading
                    )

                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = PrimaryBlue) },
                        shape = RoundedCornerShape(16.dp),
                        enabled = bankState !is BankState.Loading
                    )

                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("Account Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        enabled = bankState !is BankState.Loading
                    )

                    OutlinedTextField(
                        value = ifscCode,
                        onValueChange = { ifscCode = it },
                        label = { Text("IFSC Code") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        enabled = bankState !is BankState.Loading
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("UPI ID (Optional)") },
                        placeholder = { Text("username@bank") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Rounded.QrCode, contentDescription = null, tint = PrimaryBlue) },
                        shape = RoundedCornerShape(16.dp),
                        enabled = bankState !is BankState.Loading
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SaaSButton(
                text = "Save Details",
                onClick = {
                    bankViewModel.saveBankAccount(
                        accountHolderName = accountHolderName,
                        accountNumber = accountNumber,
                        ifscCode = ifscCode,
                        bankName = bankName,
                        upiId = upiId
                    )
                },
                isLoading = bankState is BankState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryBlue.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    "Your bank details are encrypted and securely stored. We never share them with third parties.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryBlue,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
