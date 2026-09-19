package com.company.hostaldekho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaytmScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paytm Payment", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF00B9F1))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Paytm Success Logo Mockup
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFF00B9F1), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(60.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Payment Successful", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002E6E))
            Text("to HostelDekho", fontSize = 16.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Amount Paid", color = Color.Gray)
                        Text("₹8,500", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Transaction ID", color = Color.Gray)
                        Text("PAYTM123456789", fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002E6E))
            ) {
                Text("Back to Home", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}
