package com.nutritrack.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "water_logs",
    indices = [
        Index(value = ["date"])
    ]
)
data class WaterLogEntity(
    @PrimaryKey
    val id: String,
    val date: String, // yyyy-MM-dd
    val timestamp: Long,
    val amountMl: Int
)
