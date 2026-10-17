package com.nutritrack.app.feature.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.core.common.DateUtils
import com.nutritrack.app.domain.model.DiaryEntry
import com.nutritrack.app.domain.model.FoodItem
import com.nutritrack.app.domain.model.MealType
import com.nutritrack.app.domain.repository.DiaryRepository
import com.nutritrack.app.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class AddFoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeTab = MutableStateFlow(AddFoodTab.ALL)
    private val _selectedMeal = MutableStateFlow(AddFoodUiState.defaultMealForTime())
    private val _lastAddedEntry = MutableStateFlow<DiaryEntry?>(null)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    // Debounced query stream (200ms debounce as per F-LOG-02)
    private val debouncedQuery = _searchQuery
        .debounce(200)
        .distinctUntilChanged()

    // Combined query & tab stream to fetch appropriate food items
    private val foodListFlow = combine(debouncedQuery, _activeTab) { query, tab ->
        query to tab
    }.flatMapLatest { (query, tab) ->
        val trimmed = query.trim()
        if (trimmed.isNotEmpty()) {
            foodRepository.searchFoods(trimmed)
        } else {
            when (tab) {
                AddFoodTab.RECENT -> foodRepository.getRecentFoods(30)
                AddFoodTab.FAVORITES -> foodRepository.getFavoriteFoods()
                AddFoodTab.MY_FOODS -> foodRepository.getUserCustomFoods()
                AddFoodTab.ALL -> foodRepository.getAllFoods()
            }
        }
    }

    val uiState: StateFlow<AddFoodUiState> = combine(
        _searchQuery,
        _activeTab,
        _selectedMeal,
        foodListFlow,
        _lastAddedEntry,
        _snackbarMessage
    ) { params ->
        val query = params[0] as String
        val tab = params[1] as AddFoodTab
        val meal = params[2] as MealType
        @Suppress("UNCHECKED_CAST")
        val foods = params[3] as List<FoodItem>
        val lastAdded = params[4] as? DiaryEntry
        val snackbarMsg = params[5] as? String

        AddFoodUiState(
            searchQuery = query,
            activeTab = tab,
            selectedMeal = meal,
            foodResults = foods,
            isSearching = query.trim().isNotEmpty(),
            lastAddedEntry = lastAdded,
            snackbarMessage = snackbarMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddFoodUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onTabSelected(tab: AddFoodTab) {
        _activeTab.value = tab
    }

    fun onMealSelected(meal: MealType) {
        _selectedMeal.value = meal
    }

    fun setInitialMeal(meal: MealType) {
        _selectedMeal.value = meal
    }

    fun toggleFavorite(food: FoodItem) {
        viewModelScope.launch {
            foodRepository.toggleFavorite(food.id, !food.isFavorite)
        }
    }

    fun quickAddFood(food: FoodItem, targetDate: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val entry = DiaryEntry(
                id = UUID.randomUUID().toString(),
                date = DateUtils.toIsoDate(targetDate),
                meal = _selectedMeal.value,
                foodId = food.id,
                nameSnapshot = food.name,
                emojiSnapshot = food.emoji,
                servingLabelSnapshot = food.servingLabel,
                quantity = 1.0,
                calories = food.calories,
                protein = food.protein,
                carbs = food.carbs,
                fat = food.fat,
                fiber = food.fiber,
                sugar = food.sugar,
                sodiumMg = food.sodiumMg,
                isQuickAdd = false,
                createdAt = System.currentTimeMillis()
            )

            diaryRepository.addEntry(entry)
            foodRepository.markFoodUsed(food.id)

            _lastAddedEntry.value = entry
            _snackbarMessage.value = "Added to ${_selectedMeal.value.displayName}"
        }
    }

    fun undoQuickAdd() {
        val entry = _lastAddedEntry.value ?: return
        viewModelScope.launch {
            diaryRepository.deleteEntry(entry.id)
            _lastAddedEntry.value = null
            _snackbarMessage.value = "Removed ${entry.nameSnapshot}"
        }
    }

    fun addQuickCalories(
        name: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        targetDate: LocalDate = LocalDate.now()
    ) {
        viewModelScope.launch {
            val entry = DiaryEntry(
                id = UUID.randomUUID().toString(),
                date = DateUtils.toIsoDate(targetDate),
                meal = _selectedMeal.value,
                foodId = null,
                nameSnapshot = name,
                emojiSnapshot = "⚡",
                servingLabelSnapshot = "1 serving",
                quantity = 1.0,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                isQuickAdd = true,
                createdAt = System.currentTimeMillis()
            )
            diaryRepository.addEntry(entry)
            _lastAddedEntry.value = entry
            _snackbarMessage.value = "Added to ${_selectedMeal.value.displayName}"
        }
    }

    fun saveCustomFood(
        food: FoodItem,
        logMeal: MealType?,
        targetDate: LocalDate = LocalDate.now()
    ) {
        viewModelScope.launch {
            foodRepository.saveFood(food)
            if (logMeal != null) {
                val entry = DiaryEntry(
                    id = UUID.randomUUID().toString(),
                    date = DateUtils.toIsoDate(targetDate),
                    meal = logMeal,
                    foodId = food.id,
                    nameSnapshot = food.name,
                    emojiSnapshot = food.emoji,
                    servingLabelSnapshot = food.servingLabel,
                    quantity = 1.0,
                    calories = food.calories,
                    protein = food.protein,
                    carbs = food.carbs,
                    fat = food.fat,
                    fiber = food.fiber,
                    sugar = food.sugar,
                    sodiumMg = food.sodiumMg,
                    isQuickAdd = false,
                    createdAt = System.currentTimeMillis()
                )
                diaryRepository.addEntry(entry)
                foodRepository.markFoodUsed(food.id)
                _lastAddedEntry.value = entry
                _snackbarMessage.value = "Added to ${logMeal.displayName}"
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}

