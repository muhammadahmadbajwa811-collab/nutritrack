package com.nutritrack.app.feature.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.core.common.DateUtils
import com.nutritrack.app.domain.calc.ServingCalculator
import com.nutritrack.app.domain.model.DiaryEntry
import com.nutritrack.app.domain.model.FoodItem
import com.nutritrack.app.domain.model.MealType
import com.nutritrack.app.domain.model.ServingOption
import com.nutritrack.app.domain.repository.DiaryRepository
import com.nutritrack.app.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FoodDetailViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodDetailUiState())
    val uiState: StateFlow<FoodDetailUiState> = _uiState.asStateFlow()

    fun loadFood(foodId: String, meal: MealType = MealType.LUNCH, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedMeal = meal, targetDate = date) }
            val food = foodRepository.getFoodByIdSync(foodId)
            if (food != null) {
                val baseOption = ServingOption(
                    id = "base",
                    foodId = food.id,
                    label = food.servingLabel,
                    multiplier = 1.0
                )
                val allOptions = listOf(baseOption) + food.servingOptions

                val computed = ServingCalculator.computeNutrients(
                    baseCalories = food.calories,
                    baseProtein = food.protein,
                    baseCarbs = food.carbs,
                    baseFat = food.fat,
                    baseFiber = food.fiber,
                    baseSugar = food.sugar,
                    baseSodiumMg = food.sodiumMg,
                    servingMultiplier = baseOption.multiplier,
                    quantity = 1.0
                )

                _uiState.update {
                    it.copy(
                        food = food,
                        servingOptions = allOptions,
                        selectedServingOption = baseOption,
                        quantity = 1.0f,
                        computedNutrition = computed,
                        isFavorite = food.isFavorite,
                        isEditingExistingEntry = false,
                        editingEntryId = null,
                        isLoading = false,
                        isSaved = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadForEditing(entry: DiaryEntry) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val food = entry.foodId?.let { foodRepository.getFoodByIdSync(it) }

            val baseOption = ServingOption(
                id = "base",
                foodId = food?.id ?: "",
                label = entry.servingLabelSnapshot,
                multiplier = 1.0
            )
            val allOptions = if (food != null) {
                listOf(baseOption) + food.servingOptions
            } else {
                listOf(baseOption)
            }

            val computed = ServingCalculator.computeNutrients(
                baseCalories = entry.calories / entry.quantity,
                baseProtein = entry.protein / entry.quantity,
                baseCarbs = entry.carbs / entry.quantity,
                baseFat = entry.fat / entry.quantity,
                baseFiber = entry.fiber?.let { it / entry.quantity },
                baseSugar = entry.sugar?.let { it / entry.quantity },
                baseSodiumMg = entry.sodiumMg?.let { it / entry.quantity },
                servingMultiplier = 1.0,
                quantity = entry.quantity
            )

            _uiState.update {
                it.copy(
                    food = food,
                    servingOptions = allOptions,
                    selectedServingOption = baseOption,
                    quantity = entry.quantity.toFloat(),
                    selectedMeal = entry.meal,
                    targetDate = DateUtils.parseIsoDate(entry.date),
                    computedNutrition = computed,
                    isFavorite = food?.isFavorite ?: false,
                    isEditingExistingEntry = true,
                    editingEntryId = entry.id,
                    isLoading = false,
                    isSaved = false
                )
            }
        }
    }

    fun onServingSelected(option: ServingOption) {
        _uiState.update { state ->
            val food = state.food ?: return@update state
            val computed = ServingCalculator.computeNutrients(
                baseCalories = food.calories,
                baseProtein = food.protein,
                baseCarbs = food.carbs,
                baseFat = food.fat,
                baseFiber = food.fiber,
                baseSugar = food.sugar,
                baseSodiumMg = food.sodiumMg,
                servingMultiplier = option.multiplier,
                quantity = state.quantity.toDouble()
            )
            state.copy(
                selectedServingOption = option,
                computedNutrition = computed
            )
        }
    }

    fun onQuantityChanged(qty: Float) {
        _uiState.update { state ->
            val food = state.food ?: return@update state
            val multiplier = state.selectedServingOption?.multiplier ?: 1.0
            val computed = ServingCalculator.computeNutrients(
                baseCalories = food.calories,
                baseProtein = food.protein,
                baseCarbs = food.carbs,
                baseFat = food.fat,
                baseFiber = food.fiber,
                baseSugar = food.sugar,
                baseSodiumMg = food.sodiumMg,
                servingMultiplier = multiplier,
                quantity = qty.toDouble()
            )
            state.copy(
                quantity = qty,
                computedNutrition = computed
            )
        }
    }

    fun onMealSelected(meal: MealType) {
        _uiState.update { it.copy(selectedMeal = meal) }
    }

    fun toggleFavorite() {
        val food = _uiState.value.food ?: return
        viewModelScope.launch {
            foodRepository.toggleFavorite(food.id, !food.isFavorite)
            _uiState.update { it.copy(isFavorite = !food.isFavorite) }
        }
    }

    fun saveToDiary(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val state = _uiState.value
            val food = state.food ?: return@launch
            val serving = state.selectedServingOption?.label ?: food.servingLabel
            val nutrition = state.computedNutrition

            if (state.isEditingExistingEntry && state.editingEntryId != null) {
                val updatedEntry = DiaryEntry(
                    id = state.editingEntryId,
                    date = DateUtils.toIsoDate(state.targetDate),
                    meal = state.selectedMeal,
                    foodId = food.id,
                    nameSnapshot = food.name,
                    emojiSnapshot = food.emoji,
                    servingLabelSnapshot = serving,
                    quantity = state.quantity.toDouble(),
                    calories = nutrition.calories,
                    protein = nutrition.protein,
                    carbs = nutrition.carbs,
                    fat = nutrition.fat,
                    fiber = nutrition.fiber,
                    sugar = nutrition.sugar,
                    sodiumMg = nutrition.sodiumMg,
                    isQuickAdd = false
                )
                diaryRepository.updateEntry(updatedEntry)
            } else {
                val newEntry = DiaryEntry(
                    id = UUID.randomUUID().toString(),
                    date = DateUtils.toIsoDate(state.targetDate),
                    meal = state.selectedMeal,
                    foodId = food.id,
                    nameSnapshot = food.name,
                    emojiSnapshot = food.emoji,
                    servingLabelSnapshot = serving,
                    quantity = state.quantity.toDouble(),
                    calories = nutrition.calories,
                    protein = nutrition.protein,
                    carbs = nutrition.carbs,
                    fat = nutrition.fat,
                    fiber = nutrition.fiber,
                    sugar = nutrition.sugar,
                    sodiumMg = nutrition.sodiumMg,
                    isQuickAdd = false,
                    createdAt = System.currentTimeMillis()
                )
                diaryRepository.addEntry(newEntry)
                foodRepository.markFoodUsed(food.id)
            }

            _uiState.update { it.copy(isSaved = true) }
            onSuccess()
        }
    }

    fun deleteEntry(onSuccess: () -> Unit = {}) {
        val entryId = _uiState.value.editingEntryId ?: return
        viewModelScope.launch {
            diaryRepository.deleteEntry(entryId)
            onSuccess()
        }
    }
}
