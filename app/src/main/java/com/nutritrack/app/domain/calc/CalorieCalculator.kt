package com.nutritrack.app.domain.calc

import com.nutritrack.app.domain.model.Gender
import com.nutritrack.app.domain.model.GoalType
import com.nutritrack.app.domain.model.MacroPreset
import com.nutritrack.app.domain.model.MacroSplit
import com.nutritrack.app.domain.model.UserProfile

object CalorieCalculator {

    const val MINIMUM_CALORIE_FLOOR = 1200

    /**
     * Calculates Basal Metabolic Rate (BMR) using Mifflin-St Jeor.
     */
    fun calculateBmr(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        gender: Gender
    ): Double {
        return BmrCalculator.calculateBmr(weightKg, heightCm, age, gender)
    }

    /**
     * Calculates Total Daily Energy Expenditure (TDEE).
     */
    fun calculateTdee(
        bmr: Double,
        multiplier: Double
    ): Double {
        return TdeeCalculator.calculateTdee(bmr, multiplier)
    }

    /**
     * Calculates daily calorie target honoring user profile and goal safety limits.
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
        val result = CalorieGoalCalculator.calculateTarget(
            tdee = tdee,
            goalType = profile.goal,
            paceKgPerWeek = profile.paceKgPerWeek,
            gender = profile.gender
        )

        return result.targetCalories
    }

    /**
     * Calculates macronutrient breakdown (default Balanced 50/25/25 or custom preset).
     */
    fun calculateMacroSplit(totalCalories: Int, preset: MacroPreset = MacroPreset.HIGH_PROTEIN): MacroSplit {
        // High protein preset defaults to 40% carbs, 30% protein, 30% fat per Phase 0 spec
        return MacroCalculator.calculateMacroSplit(totalCalories, preset)
    }

    /**
     * Calculates Body Mass Index (BMI).
     */
    fun calculateBmi(weightKg: Double, heightCm: Double): Double {
        return BmiCalculator.calculateBmi(weightKg, heightCm)
    }

    /**
     * Calculates recommended daily water intake in milliliters (weightKg * 35ml).
     */
    fun calculateDailyWaterGoalMl(weightKg: Double): Int {
        return WaterGoalCalculator.calculateDailyWaterGoalMl(weightKg)
    }
}
