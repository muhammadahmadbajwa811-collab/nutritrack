package com.nutritrack.app.domain.calc

import com.nutritrack.app.domain.model.ActivityLevel

object StepCalculator {
    const val CALORIES_PER_STEP_PER_KG = 0.00057
    const val STRIDE_RATIO_OF_HEIGHT = 0.415 // Stride length ≈ 41.5% of height

    /**
     * Default step goal based on user's activity level.
     */
    fun getDefaultStepGoal(activityLevel: ActivityLevel): Int {
        return activityLevel.defaultStepGoal
    }

    /**
     * Calories burned from walking steps:
     * stepKcal = steps * weightKg * 0.00057
     */
    fun calculateCaloriesBurned(steps: Int, weightKg: Double): Double {
        if (steps <= 0 || weightKg <= 0.0) return 0.0
        return steps * weightKg * CALORIES_PER_STEP_PER_KG
    }

    /**
     * Distance walked in kilometers:
     * distanceKm = steps * (heightCm * 0.415) / 100,000
     */
    fun calculateDistanceKm(steps: Int, heightCm: Double): Double {
        if (steps <= 0 || heightCm <= 0.0) return 0.0
        val strideLengthCm = heightCm * STRIDE_RATIO_OF_HEIGHT
        return (steps * strideLengthCm) / 100000.0
    }
}
