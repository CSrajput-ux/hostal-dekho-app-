package com.company.hostaldekho.data.payment

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Bridges Razorpay's activity callback to the active Compose booking screen. */
object PaymentResultBus {
    sealed interface Result {
        data class Success(val paymentId: String, val signature: String) : Result
        data class Failure(val code: Int, val message: String?) : Result
    }

    private val _results = MutableSharedFlow<Result>(extraBufferCapacity = 1)
    val results = _results.asSharedFlow()

    fun emit(result: Result) {
        _results.tryEmit(result)
    }
}
