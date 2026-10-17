package com.nutritrack.app.feature.food

import com.nutritrack.app.domain.model.DiaryEntry
import com.nutritrack.app.domain.model.FoodItem
import com.nutritrack.app.domain.model.MealType
import java.time.LocalTime

enum class AddFoodTab(val title: String) {
    RECENT("Recent"),
    FAVORITES("Favourites"),
    MY_FOODS("My foods"),
    ALL("All")
}

data class AddFoodUiState(
    val searchQuery: String = "",
    val activeTab: AddFoodTab = AddFoodTab.ALL,
    val selectedMeal: MealType = defaultMealForTime(LocalTime.now()),
    val foodResults: List<FoodItem> = emptyList(),
    val isSearching: Boolean = false,
    val lastAddedEntry: DiaryEntry? = null,
    val snackbarMessage: String? = null
) {
    companion object {
        fun defaultMealForTime(time: LocalTime = LocalTime.now()): MealType {
            return when {
                time.hour < 11 -> MealType.BREAKFAST
                time.hour < 16 -> MealType.LUNCH
                time.hour < 21 -> MealType.DINNER
                else -> MealType.SNACKS
            }
        }
    }
}
