package com.nutritrack.app.core.database.entity

import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "foods_fts")
@Fts4(contentEntity = FoodEntity::class)
data class FoodFtsEntity(
    val name: String,
    val aliases: String
)
