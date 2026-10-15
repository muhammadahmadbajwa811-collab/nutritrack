package com.nutritrack.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "foods",
    indices = [
        Index(value = ["name"]),
        Index(value = ["category"]),
        Index(value = ["barcode"]),
        Index(value = ["isFavorite"])
    ]
)
data class FoodEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val aliases: String = "", // Comma or space separated keywords: Urdu script, Roman Urdu, English
    val category: String,
    val emoji: String,
    val servingLabel: String,
    val servingGrams: Double? = null,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodiumMg: Double? = null,
    val barcode: String? = null,
    val source: String = "SEED", // "SEED", "USER", "ONLINE"
    val isFavorite: Boolean = false,
    val lastUsedAt: Long? = null,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
