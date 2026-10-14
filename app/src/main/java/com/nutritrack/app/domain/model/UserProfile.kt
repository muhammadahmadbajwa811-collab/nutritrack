package com.nutritrack.app.domain.model

enum class Gender {
    MALE,
    FEMALE
}

enum class ActivityLevel(val multiplier: Double) {
    SEDENTARY(1.2),
    LIGHTLY_ACTIVE(1.375),
    MODERATELY_ACTIVE(1.55),
    VERY_ACTIVE(1.725),
    EXTRA_ACTIVE(1.9)
}

enum class Goal(val calorieAdjustment: Int) {
    LOSE_WEIGHT(-500),
    MAINTAIN(0),
    GAIN_WEIGHT(500)
}

data class MacroSplit(
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int
)

data class UserProfile(
    val age: Int,
    val gender: Gender,
    val heightCm: Double,
    val weightKg: Double,
    val activityLevel: ActivityLevel,
    val goal: Goal,
    val customCalorieTarget: Int? = null
)
