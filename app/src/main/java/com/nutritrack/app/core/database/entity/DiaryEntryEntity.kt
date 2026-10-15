package com.nutritrack.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "diary_entries",
    indices = [
        Index(value = ["date"]),
        Index(value = ["foodId"])
    ]
)
data class DiaryEntryEntity(
    @PrimaryKey
    val id: String,
    val date: String, // ISO date format: yyyy-MM-dd
    val meal: String, // "BREAKFAST", "LUNCH", "DINNER", "SNACKS"
    val foodId: String? = null,
    val nameSnapshot: String,
    val emojiSnapshot: String,
    val servingLabelSnapshot: String,
    val quantity: Double,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodiumMg: Double? = null,
    val isQuickAdd: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
