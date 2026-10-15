package com.nutritrack.app.domain.model

data class WaterEntry(
    val id: String,
    val date: String, // yyyy-MM-dd
    val timestamp: Long,
    val amountMl: Int
)
