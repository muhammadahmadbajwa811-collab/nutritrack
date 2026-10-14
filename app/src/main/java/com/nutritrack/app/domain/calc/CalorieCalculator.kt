package com.nutritrack.app.domain.calc

import com.nutritrack.app.domain.model.Gender
import com.nutritrack.app.domain.model.Goal
import com.nutritrack.app.domain.model.MacroSplit
import com.nutritrack.app.domain.model.UserProfile
import kotlin.math.roundToInt

object CalorieCalculator {

    const val MINIMUM_CALORIE_FLOOR = 1200

    /**
     * Calculates Basal Metabolic Rate (BMR) using the Mifflin-St Jeor equation.
     */
    fun calculateBmr(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        gender: Gender
    ): Double {
        val base = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age)
        return when (gender) {
            Gender.MALE -> base + 5.0
            Gender.FEMALE -> base - 161.0
        }
    }

    /**
     * Calculates Total Daily Energy Expenditure (TDEE).
     */
    fun calculateTdee(
        bmr: Double,
        multiplier: Double
    ): Double {
        return bmr * multiplier
    }

    /**
     * Calculates recommended daily calorie target based on user profile and fitness goal.
     */
    fun calculateDailyCalorieTarget(profile: UserProfile): Int {
        if (profile.customCalorieTarget != null && profile.customCalorieTarget > 0) {
            return profile.customCalorieTarget
        }

        val bmr = calculateBmr(
            weightKg = profile.weightKg,
            heightCm = profile.heightCm,
            age = profile.age,
            gender = profile.gender
        )

        val tdee = calculateTdee(bmr, profile.activityLevel.multiplier)
        val target = (tdee + profile.goal.calorieAdjustment).roundToInt()

        return if (profile.goal == Goal.LOSE_WEIGHT) {
            maxOf(MINIMUM_CALORIE_FLOOR, target)
        } else {
            target
        }
    }

    /**
     * Calculates standard macronutrient breakdown:
     * - Protein: 30% of total calories (4 kcal/g)
     * - Carbs: 40% of total calories (4 kcal/g)
     * - Fat: 30% of total calories (9 kcal/g)
     */
    fun calculateMacroSplit(totalCalories: Int): MacroSplit {
        val proteinCalories = totalCalories * 0.30
        val carbsCalories = totalCalories * 0.40
        val fatCalories = totalCalories * 0.30

        return MacroSplit(
            proteinGrams = (proteinCalories / 4.0).roundToInt(),
            carbsGrams = (carbsCalories / 4.0).roundToInt(),
            fatGrams = (fatCalories / 9.0).roundToInt()
        )
    }

    /**
     * Calculates Body Mass Index (BMI).
     */
    fun calculateBmi(weightKg: Double, heightCm: Double): Double {
        val heightM = heightCm / 100.0
        if (heightM <= 0.0) return 0.0
        return weightKg / (heightM * heightM)
    }

    /**
     * Calculates recommended daily water intake in milliliters (35ml per kg body weight).
     */
    fun calculateDailyWaterGoalMl(weightKg: Double): Int {
        return (weightKg * 35.0).roundToInt()
    }
}
