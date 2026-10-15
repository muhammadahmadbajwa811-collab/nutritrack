package com.nutritrack.app.domain.calc

import com.nutritrack.app.domain.model.GoalType
import java.time.LocalDate
import kotlin.math.abs

data class WeightDataPoint(
    val date: String, // yyyy-MM-dd
    val weightKg: Double
)

data class WeightTrendResult(
    val currentAverageKg: Double,
    val totalChangeKg: Double,
    val projectedGoalDate: LocalDate?,
    val isMovingTowardGoal: Boolean
)

object WeightTrendCalculator {

    /**
     * Calculates 7-day moving average for a series of weight data points.
     */
    fun calculateMovingAverage(entries: List<WeightDataPoint>, windowDays: Int = 7): List<Pair<String, Double>> {
        if (entries.isEmpty()) return emptyList()

        val sorted = entries.sortedBy { it.date }
        val result = mutableListOf<Pair<String, Double>>()

        for (i in sorted.indices) {
            val fromIndex = maxOf(0, i - windowDays + 1)
            val window = sorted.subList(fromIndex, i + 1)
            val avg = window.map { it.weightKg }.average()
            result.add(Pair(sorted[i].date, avg))
        }

        return result
    }

    /**
     * Calculates projected goal arrival date and adherence to target weight.
     * projected goal date = remainingKg / paceKgPerWeek weeks from today.
     * Hidden (null) if the trend is moving away from the goal.
     */
    fun calculateTrendAndProjection(
        currentWeightKg: Double,
        startWeightKg: Double,
        goalWeightKg: Double,
        goalType: GoalType,
        paceKgPerWeek: Double,
        today: LocalDate = LocalDate.now()
    ): WeightTrendResult {
        val totalChange = currentWeightKg - startWeightKg
        val remainingKg = abs(currentWeightKg - goalWeightKg)

        val isMovingTowardGoal = when (goalType) {
            GoalType.LOSE_WEIGHT -> currentWeightKg <= startWeightKg
            GoalType.GAIN_WEIGHT -> currentWeightKg >= startWeightKg
            GoalType.MAINTAIN_WEIGHT -> remainingKg <= 1.0
        }

        val projectedGoalDate: LocalDate? = if (isMovingTowardGoal && paceKgPerWeek > 0.0 && remainingKg > 0.0) {
            val weeksRemaining = remainingKg / paceKgPerWeek
            val daysRemaining = (weeksRemaining * 7.0).toLong()
            today.plusDays(daysRemaining)
        } else if (remainingKg <= 0.1) {
            today
        } else {
            null
        }

        return WeightTrendResult(
            currentAverageKg = currentWeightKg,
            totalChangeKg = totalChange,
            projectedGoalDate = projectedGoalDate,
            isMovingTowardGoal = isMovingTowardGoal
        )
    }
}
