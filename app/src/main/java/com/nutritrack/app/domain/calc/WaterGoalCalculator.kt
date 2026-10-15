package com.nutritrack.app.domain.calc

object WaterGoalCalculator {
    const val MINIMUM_WATER_GOAL_ML = 1500
    const val ML_PER_KG = 35.0
    const val ROUNDING_GRID_ML = 250

    /**
     * Calculates recommended daily water intake in milliliters:
     * weightKg * 35 ml, rounded to the nearest 250 ml, minimum 1,500 ml.
     */
    fun calculateDailyWaterGoalMl(weightKg: Double): Int {
        val rawMl = weightKg * ML_PER_KG
        // Round to nearest 250 ml
        val roundedMl = (Math.round(rawMl / ROUNDING_GRID_ML.toDouble()) * ROUNDING_GRID_ML).toInt()
        return maxOf(MINIMUM_WATER_GOAL_ML, roundedMl)
    }
}
