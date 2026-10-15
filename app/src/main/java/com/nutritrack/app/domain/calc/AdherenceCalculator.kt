package com.nutritrack.app.domain.calc

object AdherenceCalculator {
    const val LOWER_BOUND_RATIO = 0.90 // 90%
    const val UPPER_BOUND_RATIO = 1.10 // 110%

    /**
     * Checks if the calories eaten for a day are within the target window (90% to 110%).
     */
    fun isWithinGoal(eatenCalories: Int, goalCalories: Int): Boolean {
        if (goalCalories <= 0) return false
        val minAllowed = goalCalories * LOWER_BOUND_RATIO
        val maxAllowed = goalCalories * UPPER_BOUND_RATIO
        return eatenCalories.toDouble() in minAllowed..maxAllowed
    }

    /**
     * Calculates adherence rate (percentage of days within goal).
     */
    fun calculateAdherenceRate(daysWithinGoal: Int, totalDays: Int): Double {
        if (totalDays <= 0) return 0.0
        return (daysWithinGoal.toDouble() / totalDays.toDouble()) * 100.0
    }
}
