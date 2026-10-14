package com.nutritrack.app.domain.usecase

import com.nutritrack.app.core.datastore.NutriTrackPreferences
import com.nutritrack.app.domain.calc.CalorieCalculator
import com.nutritrack.app.domain.model.MacroSplit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

data class DaySummary(
    val calorieTarget: Int,
    val caloriesConsumed: Int,
    val caloriesBurned: Int,
    val netCalories: Int,
    val macroSplit: MacroSplit,
    val waterMl: Int,
    val steps: Int
)

class GetDaySummaryUseCase @Inject constructor(
    private val preferences: NutriTrackPreferences
) {
    operator fun invoke(): Flow<DaySummary> {
        return preferences.dailyCalorieGoal.combine(flowOf(0)) { goal, _ ->
            val consumed = 0
            val burned = 0
            val net = consumed - burned
            val macros = CalorieCalculator.calculateMacroSplit(goal)
            DaySummary(
                calorieTarget = goal,
                caloriesConsumed = consumed,
                caloriesBurned = burned,
                netCalories = net,
                macroSplit = macros,
                waterMl = 0,
                steps = 0
            )
        }
    }
}
