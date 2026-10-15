package com.nutritrack.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "serving_options",
    foreignKeys = [
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["foodId"])
    ]
)
data class ServingOptionEntity(
    @PrimaryKey
    val id: String,
    val foodId: String,
    val label: String,
    val multiplier: Double
)
