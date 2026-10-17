package com.nutritrack.app.feature.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.core.common.DateUtils
import com.nutritrack.app.domain.model.DiaryEntry
import com.nutritrack.app.domain.model.MealType
import com.nutritrack.app.domain.repository.DiaryRepository
import com.nutritrack.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _collapsedMeals = MutableStateFlow<Set<MealType>>(emptySet())
    private val _isClearDayDialogOpen = MutableStateFlow(false)
    private val _recentlyDeletedEntry = MutableStateFlow<DiaryEntry?>(null)
    private val _clearedEntries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    private val _userMessage = MutableStateFlow<String?>(null)

    private val userProfileFlow = userRepository.getUserProfile()

    private val entriesForDateFlow = _selectedDate.flatMapLatest { date ->
        diaryRepository.getEntriesForDate(DateUtils.toIsoDate(date))
    }

    val uiState: StateFlow<DiaryUiState> = combine(
        _selectedDate,
        userProfileFlow,
        entriesForDateFlow,
        _collapsedMeals,
        _isClearDayDialogOpen,
        _recentlyDeletedEntry,
        _userMessage
    ) { params ->
        val date = params[0] as LocalDate
        val profile = params[1] as? com.nutritrack.app.domain.model.UserProfile
        @Suppress("UNCHECKED_CAST")
        val entries = params[2] as List<DiaryEntry>
        @Suppress("UNCHECKED_CAST")
        val collapsed = params[3] as Set<MealType>
        val isClearDayOpen = params[4] as Boolean
        val recentlyDeleted = params[5] as? DiaryEntry
        val userMsg = params[6] as? String

        val totalKcal = entries.sumOf { it.calories }.roundToInt()
        val totalProtein = entries.sumOf { it.protein }.roundToInt()
        val totalCarbs = entries.sumOf { it.carbs }.roundToInt()
        val totalFat = entries.sumOf { it.fat }.roundToInt()

        val grouped = MealType.values().associateWith { meal ->
            entries.filter { it.meal == meal }
        }

        DiaryUiState(
            selectedDate = date,
            today = LocalDate.now(),
            formattedDate = DateUtils.formatDisplayDate(date),
            entriesByMeal = grouped,
            totalCalories = totalKcal,
            calorieGoal = profile?.calorieGoal ?: 2000,
            totalProtein = totalProtein,
            proteinGoal = profile?.proteinGoalG ?: 125,
            totalCarbs = totalCarbs,
            carbsGoal = profile?.carbsGoalG ?: 250,
            totalFat = totalFat,
            fatGoal = profile?.fatGoalG ?: 55,
            collapsedMeals = collapsed,
            isClearDayDialogOpen = isClearDayOpen,
            recentlyDeletedEntry = recentlyDeleted,
            userMessage = userMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DiaryUiState()
    )

    fun onDateSelected(date: LocalDate) {
        if (date <= LocalDate.now()) {
            _selectedDate.value = date
        }
    }

    fun onNextDay() {
        val next = _selectedDate.value.plusDays(1)
        if (next <= LocalDate.now()) {
            _selectedDate.value = next
        }
    }

    fun onPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun onTodayClick() {
        _selectedDate.value = LocalDate.now()
    }

    fun toggleMealCollapse(meal: MealType) {
        _collapsedMeals.update { set ->
            if (set.contains(meal)) set - meal else set + meal
        }
    }

    fun deleteEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            _recentlyDeletedEntry.value = entry
            diaryRepository.deleteEntry(entry.id)
            _userMessage.value = "Removed ${entry.nameSnapshot}"
        }
    }

    fun undoDelete() {
        val entryToRestore = _recentlyDeletedEntry.value ?: return
        viewModelScope.launch {
            diaryRepository.addEntry(entryToRestore)
            _recentlyDeletedEntry.value = null
            _userMessage.value = "Restored ${entryToRestore.nameSnapshot}"
        }
    }

    fun showClearDayDialog() {
        _isClearDayDialogOpen.value = true
    }

    fun dismissClearDayDialog() {
        _isClearDayDialogOpen.value = false
    }

    fun clearDay() {
        viewModelScope.launch {
            val dateIso = DateUtils.toIsoDate(_selectedDate.value)
            val currentEntries = diaryRepository.getEntriesForDateSync(dateIso)
            _clearedEntries.value = currentEntries
            diaryRepository.clearDate(dateIso)
            _isClearDayDialogOpen.value = false
            _userMessage.value = "Cleared all entries for ${DateUtils.formatDisplayDate(_selectedDate.value)}"
        }
    }

    fun undoClearDay() {
        val toRestore = _clearedEntries.value
        if (toRestore.isNotEmpty()) {
            viewModelScope.launch {
                diaryRepository.addEntries(toRestore)
                _clearedEntries.value = emptyList()
                _userMessage.value = "Restored ${toRestore.size} entries"
            }
        }
    }

    fun copyFromYesterday(targetMeal: MealType? = null) {
        viewModelScope.launch {
            val yesterdayIso = DateUtils.toIsoDate(_selectedDate.value.minusDays(1))
            val todayIso = DateUtils.toIsoDate(_selectedDate.value)
            val yesterdayEntries = diaryRepository.getEntriesForDateSync(yesterdayIso)

            val filtered = if (targetMeal != null) {
                yesterdayEntries.filter { it.meal == targetMeal }
            } else {
                yesterdayEntries
            }

            if (filtered.isEmpty()) {
                _userMessage.value = "No entries found from yesterday"
                return@launch
            }

            val clonedEntries = filtered.map { original ->
                original.copy(
                    id = UUID.randomUUID().toString(),
                    date = todayIso,
                    createdAt = System.currentTimeMillis()
                )
            }

            diaryRepository.addEntries(clonedEntries)
            _userMessage.value = "Copied ${clonedEntries.size} items from yesterday"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
