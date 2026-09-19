package com.company.hostaldekho

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.company.hostaldekho.navigation.NavGraph
import com.company.hostaldekho.data.payment.PaymentResultBus
import com.company.hostaldekho.ui.theme.HostelDekhoTheme
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HostelDekhoTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(navController = navController)
                }
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String, paymentData: PaymentData) {
        val paymentId = razorpayPaymentId
        val signature = paymentData.signature
        if (paymentId.isBlank() || signature.isNullOrBlank()) {
            PaymentResultBus.emit(PaymentResultBus.Result.Failure(
                code = -1, message = "Payment result was incomplete. Please contact support."
            ))
            return
        }
        PaymentResultBus.emit(PaymentResultBus.Result.Success(paymentId, signature))
    }

    override fun onPaymentError(code: Int, response: String, paymentData: PaymentData) {
        PaymentResultBus.emit(PaymentResultBus.Result.Failure(code, response))
    }
}
