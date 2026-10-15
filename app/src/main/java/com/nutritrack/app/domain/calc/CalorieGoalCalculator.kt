package com.nutritrack.app.domain.calc

import com.nutritrack.app.domain.model.Gender
import com.nutritrack.app.domain.model.GoalType
import kotlin.math.roundToInt

data class CalorieGoalResult(
    val targetCalories: Int,
    val tdee: Double,
    val paceAdjustment: Double,
    val isDeficitCapped: Boolean = false,
    val isFloorApplied: Boolean = false,
    val isCeilingApplied: Boolean = false,
    val explanation: String? = null
)

object CalorieGoalCalculator {
    const val FEMALE_FLOOR_KCAL = 1200
    const val MALE_FLOOR_KCAL = 1500
    const val MAX_CEILING_KCAL = 6000
    const val MAX_DEFICIT_RATIO = 0.25 // 25% max deficit of TDEE

    /**
     * Calculates the daily calorie target using TDEE, goal type, and pace.
     * Enforces safety limits:
     * - Deficit capped at 25% of TDEE
     * - Floor: 1,200 kcal (female) or 1,500 kcal (male)
     * - Ceiling: 6,000 kcal
     * - Rounded to nearest 10 kcal
     */
    fun calculateTarget(
        tdee: Double,
        goalType: GoalType,
        paceKgPerWeek: Double,
        gender: Gender
    ): CalorieGoalResult {
        // 1 kg body fat ≈ 7,700 kcal -> dailyDelta = (paceKgPerWeek * 7700) / 7
        val rawPaceAdjustment = (paceKgPerWeek * 7700.0) / 7.0

        var target = when (goalType) {
            GoalType.LOSE_WEIGHT -> tdee - rawPaceAdjustment
            GoalType.GAIN_WEIGHT -> tdee + rawPaceAdjustment
            GoalType.MAINTAIN_WEIGHT -> tdee
        }

        var isDeficitCapped = false
        var isFloorApplied = false
        var isCeilingApplied = false
        val explanations = mutableListOf<String>()

        if (goalType == GoalType.LOSE_WEIGHT) {
            val maxAllowedDeficit = tdee * MAX_DEFICIT_RATIO
            val minByDeficitCap = tdee - maxAllowedDeficit
            if (target < minByDeficitCap) {
                target = minByDeficitCap
                isDeficitCapped = true
                explanations.add("Deficit capped at 25% of your daily burn for safety.")
            }

            val floor = if (gender == Gender.FEMALE) FEMALE_FLOOR_KCAL else MALE_FLOOR_KCAL
            if (target < floor) {
                target = floor.toDouble()
                isFloorApplied = true
                explanations.add("Adjusted to the minimum safe calorie floor of $floor kcal.")
            }
        }

        if (target > MAX_CEILING_KCAL) {
            target = MAX_CEILING_KCAL.toDouble()
            isCeilingApplied = true
            explanations.add("Adjusted to the maximum safety ceiling of $MAX_CEILING_KCAL kcal.")
        }

        // Round to nearest 10 kcal
        val roundedTarget = (Math.round(target / 10.0) * 10).toInt()

        return CalorieGoalResult(
            targetCalories = roundedTarget,
            tdee = tdee,
            paceAdjustment = rawPaceAdjustment,
            isDeficitCapped = isDeficitCapped,
            isFloorApplied = isFloorApplied,
            isCeilingApplied = isCeilingApplied,
            explanation = if (explanations.isNotEmpty()) explanations.joinToString(" ") else null
        )
    }
}
