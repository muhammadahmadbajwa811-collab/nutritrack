package com.nutritrack.app.domain.calc

import com.nutritrack.app.domain.model.WorkoutIntensity

object WorkoutCalculator {
    const val ESTIMATED_STEPS_PER_WALK_RUN_MINUTE = 100

    /**
     * Calculates exercise calories burned from MET, weight, duration, and intensity:
     * calories = MET * 3.5 * weightKg / 200 * minutes * intensityMultiplier
     */
    fun calculateExerciseCalories(
        met: Double,
        weightKg: Double,
        minutes: Int,
        intensity: WorkoutIntensity = WorkoutIntensity.MODERATE
    ): Double {
        if (met <= 0.0 || weightKg <= 0.0 || minutes <= 0) return 0.0
        val baseKcal = (met * 3.5 * weightKg / 200.0) * minutes
        return baseKcal * intensity.multiplier
    }

    /**
     * Deduplicates step calories against walking/running workouts to avoid double counting:
     * stepKcal excluding steps from minutes already covered by walking or running workouts
     * (estimate 100 steps per minute).
     */
    fun calculateTotalBurnedCalories(
        totalWorkoutCalories: Double,
        rawStepCalories: Double,
        walkOrRunWorkoutMinutes: Int,
        weightKg: Double
    ): Double {
        val coveredSteps = walkOrRunWorkoutMinutes * ESTIMATED_STEPS_PER_WALK_RUN_MINUTE
        val coveredStepKcal = StepCalculator.calculateCaloriesBurned(coveredSteps, weightKg)
        val deduplicatedStepCalories = maxOf(0.0, rawStepCalories - coveredStepKcal)
        return totalWorkoutCalories + deduplicatedStepCalories
    }
}
