package com.nutritrack.app.feature.diary

import com.nutritrack.app.core.common.DateUtils
import com.nutritrack.app.domain.model.DiaryEntry
import com.nutritrack.app.domain.model.MealType
import java.time.LocalDate

data class DiaryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val today: LocalDate = LocalDate.now(),
    val formattedDate: String = DateUtils.formatDisplayDate(LocalDate.now()),
    val entriesByMeal: Map<MealType, List<DiaryEntry>> = mapOf(
        MealType.BREAKFAST to emptyList(),
        MealType.LUNCH to emptyList(),
        MealType.DINNER to emptyList(),
        MealType.SNACKS to emptyList()
    ),
    val totalCalories: Int = 0,
    val calorieGoal: Int = 2000,
    val totalProtein: Int = 0,
    val proteinGoal: Int = 125,
    val totalCarbs: Int = 0,
    val carbsGoal: Int = 250,
    val totalFat: Int = 0,
    val fatGoal: Int = 55,
    val collapsedMeals: Set<MealType> = emptySet(),
    val isClearDayDialogOpen: Boolean = false,
    val recentlyDeletedEntry: DiaryEntry? = null,
    val userMessage: String? = null
) {
    val isToday: Boolean get() = selectedDate == today
    fun isMealCollapsed(meal: MealType): Boolean = collapsedMeals.contains(meal)
}
