package com.nutritrack.app.domain.model

enum class Gender {
    MALE,
    FEMALE
}

enum class ActivityLevel(val multiplier: Double, val defaultStepGoal: Int) {
    SEDENTARY(1.2, 8000),
    LIGHTLY_ACTIVE(1.375, 10000),
    MODERATELY_ACTIVE(1.55, 10000),
    VERY_ACTIVE(1.725, 12000),
    EXTRA_ACTIVE(1.9, 14000)
}

enum class GoalType {
    LOSE_WEIGHT,
    MAINTAIN_WEIGHT,
    GAIN_WEIGHT
}

// Backward-compatibility alias for Goal
typealias Goal = GoalType

enum class MacroPreset(val carbsPct: Int, val proteinPct: Int, val fatPct: Int) {
    BALANCED(50, 25, 25),
    HIGH_PROTEIN(40, 30, 30),
    LOW_CARB(30, 35, 35),
    CUSTOM(0, 0, 0)
}

data class MacroSplit(
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int
)

data class UserProfile(
    val id: Long = 1L,
    val name: String = "",
    val gender: Gender = Gender.MALE,
    val birthYear: Int = 1995,
    val age: Int = 30,
    val heightCm: Double = 175.0,
    val weightKg: Double = 75.0,
    val goalWeightKg: Double = 70.0,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATELY_ACTIVE,
    val goal: GoalType = GoalType.LOSE_WEIGHT,
    val paceKgPerWeek: Double = 0.5,
    val calorieGoal: Int = 2000,
    val macroPreset: MacroPreset = MacroPreset.BALANCED,
    val proteinGoalG: Int = 125,
    val carbsGoalG: Int = 250,
    val fatGoalG: Int = 55,
    val waterGoalMl: Int = 2500,
    val stepGoal: Int = 10000,
    val customCalorieTarget: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
