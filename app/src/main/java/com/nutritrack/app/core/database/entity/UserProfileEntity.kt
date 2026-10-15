package com.nutritrack.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Long = 1L,
    val name: String,
    val gender: String, // "MALE", "FEMALE"
    val birthYear: Int,
    val heightCm: Double,
    val weightKg: Double,
    val goalWeightKg: Double,
    val activityLevel: String, // "SEDENTARY", "LIGHTLY_ACTIVE", "MODERATELY_ACTIVE", "VERY_ACTIVE", "EXTRA_ACTIVE"
    val goalType: String, // "LOSE_WEIGHT", "MAINTAIN_WEIGHT", "GAIN_WEIGHT"
    val paceKgPerWeek: Double,
    val calorieGoal: Int,
    val macroPreset: String, // "BALANCED", "HIGH_PROTEIN", "LOW_CARB", "CUSTOM"
    val proteinGoalG: Int,
    val carbsGoalG: Int,
    val fatGoalG: Int,
    val waterGoalMl: Int,
    val stepGoal: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
