package com.nutritrack.app.feature.home

import com.nutritrack.app.core.common.DateUtils
import com.nutritrack.app.domain.model.MealType
import java.time.LocalDate

data class MealTileSummary(
    val mealType: MealType,
    val calories: Int,
    val itemCount: Int
)

data class HomeUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val today: LocalDate = LocalDate.now(),
    val formattedDate: String = DateUtils.formatDisplayDate(LocalDate.now()),
    val greeting: String = "Welcome",
    val userName: String = "",
    val streakDays: Int = 0,
    val eatenCalories: Int = 0,
    val burnedCalories: Int = 0,
    val calorieGoal: Int = 2000,
    val proteinEatenG: Int = 0,
    val proteinGoalG: Int = 125,
    val carbsEatenG: Int = 0,
    val carbsGoalG: Int = 250,
    val fatEatenG: Int = 0,
    val fatGoalG: Int = 55,
    val breakfastSummary: MealTileSummary = MealTileSummary(MealType.BREAKFAST, 0, 0),
    val lunchSummary: MealTileSummary = MealTileSummary(MealType.LUNCH, 0, 0),
    val dinnerSummary: MealTileSummary = MealTileSummary(MealType.DINNER, 0, 0),
    val snacksSummary: MealTileSummary = MealTileSummary(MealType.SNACKS, 0, 0),
    val waterIntakeMl: Int = 0,
    val waterGoalMl: Int = 2500,
    val stepCount: Int = 0,
    val stepGoal: Int = 10000,
    val weekCalories: List<Int> = List(7) { 0 },
    val weekDayLabels: List<String> = listOf("M", "T", "W", "T", "F", "S", "S"),
    val todayWeekIndex: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val snackbarMessage: String? = null
) {
    val remainingCalories: Int
        get() = (calorieGoal - (eatenCalories - burnedCalories))
}
