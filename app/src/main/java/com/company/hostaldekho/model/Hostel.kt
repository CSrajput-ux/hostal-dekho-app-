package com.company.hostaldekho.model

data class Hostel(
    val id: String,
    val name: String,
    val price: String,
    val rating: Float,
    val location: String,
    val imageUrl: String,
    val category: String,
    val isVerified: Boolean = false
)
