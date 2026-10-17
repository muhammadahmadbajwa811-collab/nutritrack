package com.nutritrack.app.feature.food

import com.nutritrack.app.domain.calc.ComputedNutrition
import com.nutritrack.app.domain.model.FoodItem
import com.nutritrack.app.domain.model.MealType
import com.nutritrack.app.domain.model.ServingOption
import java.time.LocalDate

data class FoodDetailUiState(
    val food: FoodItem? = null,
    val servingOptions: List<ServingOption> = emptyList(),
    val selectedServingOption: ServingOption? = null,
    val quantity: Float = 1.0f,
    val selectedMeal: MealType = MealType.LUNCH,
    val targetDate: LocalDate = LocalDate.now(),
    val computedNutrition: ComputedNutrition = ComputedNutrition(0.0, 0.0, 0.0, 0.0),
    val isFavorite: Boolean = false,
    val isEditingExistingEntry: Boolean = false,
    val editingEntryId: String? = null,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false
)
