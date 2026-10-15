package com.nutritrack.app.domain.model

data class WeightEntry(
    val id: String,
    val date: String, // yyyy-MM-dd
    val weightKg: Double,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
