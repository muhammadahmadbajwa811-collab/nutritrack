package com.nutritrack.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workouts",
    indices = [
        Index(value = ["date"])
    ]
)
data class WorkoutEntity(
    @PrimaryKey
    val id: String,
    val date: String, // yyyy-MM-dd
    val startTime: Long? = null,
    val activityKey: String,
    val name: String,
    val minutes: Int,
    val intensity: String = "MODERATE", // "LIGHT", "MODERATE", "VIGOROUS"
    val met: Double,
    val calories: Double,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "weight_logs",
    indices = [
        Index(value = ["date"], unique = true)
    ]
)
data class WeightLogEntity(
    @PrimaryKey
    val id: String,
    val date: String, // yyyy-MM-dd
    val weightKg: Double,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val key: String,
    val unlockedAt: Long? = null,
    val progress: Int = 0
)

@Entity(tableName = "app_meta")
data class AppMetaEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
