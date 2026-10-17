package com.nutritrack.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.core.common.DateUtils
import com.nutritrack.app.core.datastore.NutriTrackPreferences
import com.nutritrack.app.domain.calc.StreakCalculator
import com.nutritrack.app.domain.model.MealType
import com.nutritrack.app.domain.repository.DiaryRepository
import com.nutritrack.app.domain.repository.StepRepository
import com.nutritrack.app.domain.repository.UserRepository
import com.nutritrack.app.domain.repository.WaterRepository
import com.nutritrack.app.domain.repository.WorkoutRepository
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
import java.time.LocalTime
import javax.inject.Inject
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val diaryRepository: DiaryRepository,
    private val waterRepository: WaterRepository,
    private val stepRepository: StepRepository,
    private val workoutRepository: WorkoutRepository,
    private val preferences: NutriTrackPreferences
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)

    // All logged dates for streak
    private val loggedDatesFlow = diaryRepository.getAllLoggedDates()

    // Observe user profile for targets & name
    private val userProfileFlow = userRepository.getUserProfile()

    // Observe entries for selected date
    private val entriesForDateFlow = _selectedDate.flatMapLatest { date ->
        diaryRepository.getEntriesForDate(DateUtils.toIsoDate(date))
    }

    // Observe water for selected date
    private val waterForDateFlow = _selectedDate.flatMapLatest { date ->
        waterRepository.getTotalWaterForDate(DateUtils.toIsoDate(date))
    }

    // Observe steps for selected date
    private val stepsForDateFlow = _selectedDate.flatMapLatest { date ->
        stepRepository.getStepDay(DateUtils.toIsoDate(date))
    }

    // Observe workouts for selected date
    private val workoutCaloriesForDateFlow = _selectedDate.flatMapLatest { date ->
        workoutRepository.getTotalWorkoutCalories(DateUtils.toIsoDate(date))
    }

    // Observe week entries for 7-day chart
    private val weekEntriesFlow = _selectedDate.flatMapLatest { date ->
        val weekDays = DateUtils.getWeekDays(date)
        val startIso = DateUtils.toIsoDate(weekDays.first())
        val endIso = DateUtils.toIsoDate(weekDays.last())
        diaryRepository.getEntriesBetweenDates(startIso, endIso)
    }

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    private var lastAddedWaterAmount: Int? = null

    val uiState: StateFlow<HomeUiState> = combine(
        _selectedDate,
        userProfileFlow,
        entriesForDateFlow,
        waterForDateFlow,
        stepsForDateFlow,
        workoutCaloriesForDateFlow,
        loggedDatesFlow,
        weekEntriesFlow,
        _isRefreshing,
        _snackbarMessage
    ) { params ->
        val date = params[0] as LocalDate
        val profile = params[1] as? com.nutritrack.app.domain.model.UserProfile
        @Suppress("UNCHECKED_CAST")
        val entries = params[2] as List<com.nutritrack.app.domain.model.DiaryEntry>
        val waterIntake = params[3] as Int
        val stepDay = params[4] as? com.nutritrack.app.domain.model.StepDaySummary
        val workoutKcal = params[5] as Double
        @Suppress("UNCHECKED_CAST")
        val loggedDates = params[6] as List<String>
        @Suppress("UNCHECKED_CAST")
        val weekEntries = params[7] as List<com.nutritrack.app.domain.model.DiaryEntry>
        val isRefreshing = params[8] as Boolean
        val snackbarMsg = params[9] as? String

        val today = LocalDate.now()
        val userName = profile?.name.orEmpty()
        val greeting = DateUtils.getGreeting(userName, LocalTime.now())

        // Calculate Streak
        val streakResult = StreakCalculator.calculateStreak(loggedDates.toSet(), today)

        // Today's Totals
        val eatenKcal = entries.sumOf { it.calories }.roundToInt()
        val proteinG = entries.sumOf { it.protein }.roundToInt()
        val carbsG = entries.sumOf { it.carbs }.roundToInt()
        val fatG = entries.sumOf { it.fat }.roundToInt()

        // Meal breakdown
        val breakfastEntries = entries.filter { it.meal == MealType.BREAKFAST }
        val lunchEntries = entries.filter { it.meal == MealType.LUNCH }
        val dinnerEntries = entries.filter { it.meal == MealType.DINNER }
        val snacksEntries = entries.filter { it.meal == MealType.SNACKS }

        // Steps burned calories
        val stepCount = stepDay?.effectiveSteps ?: 0
        val userWeight = profile?.weightKg ?: 70.0
        val stepCalories = com.nutritrack.app.domain.calc.StepCalculator.calculateCaloriesBurned(stepCount, userWeight)
        val totalBurned = (workoutKcal + stepCalories).roundToInt()

        // Week chart data (Monday to Sunday)
        val weekDays = DateUtils.getWeekDays(date)
        val weekDayLabels = weekDays.map { DateUtils.getDayOfWeekLabel(it) }
        val weekCalories = weekDays.map { day ->
            val dayIso = DateUtils.toIsoDate(day)
            weekEntries.filter { it.date == dayIso }.sumOf { it.calories }.roundToInt()
        }
        val todayWeekIndex = weekDays.indexOfFirst { it == today }.let { if (it >= 0) it else 0 }

        HomeUiState(
            selectedDate = date,
            today = today,
            formattedDate = DateUtils.formatDisplayDate(date),
            greeting = greeting,
            userName = userName,
            streakDays = streakResult.currentStreak,
            eatenCalories = eatenKcal,
            burnedCalories = totalBurned,
            calorieGoal = profile?.calorieGoal ?: 2000,
            proteinEatenG = proteinG,
            proteinGoalG = profile?.proteinGoalG ?: 125,
            carbsEatenG = carbsG,
            carbsGoalG = profile?.carbsGoalG ?: 250,
            fatEatenG = fatG,
            fatGoalG = profile?.fatGoalG ?: 55,
            breakfastSummary = MealTileSummary(
                mealType = MealType.BREAKFAST,
                calories = breakfastEntries.sumOf { it.calories }.roundToInt(),
                itemCount = breakfastEntries.size
            ),
            lunchSummary = MealTileSummary(
                mealType = MealType.LUNCH,
                calories = lunchEntries.sumOf { it.calories }.roundToInt(),
                itemCount = lunchEntries.size
            ),
            dinnerSummary = MealTileSummary(
                mealType = MealType.DINNER,
                calories = dinnerEntries.sumOf { it.calories }.roundToInt(),
                itemCount = dinnerEntries.size
            ),
            snacksSummary = MealTileSummary(
                mealType = MealType.SNACKS,
                calories = snacksEntries.sumOf { it.calories }.roundToInt(),
                itemCount = snacksEntries.size
            ),
            waterIntakeMl = waterIntake,
            waterGoalMl = profile?.waterGoalMl ?: 2500,
            stepCount = stepCount,
            stepGoal = profile?.stepGoal ?: 10000,
            weekCalories = weekCalories,
            weekDayLabels = weekDayLabels,
            todayWeekIndex = todayWeekIndex,
            isLoading = false,
            isRefreshing = isRefreshing,
            snackbarMessage = snackbarMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun onDateSelected(date: LocalDate) {
        if (date <= LocalDate.now()) {
            _selectedDate.value = date
        }
    }

    fun onTodayClick() {
        _selectedDate.value = LocalDate.now()
    }

    fun onAddWater(amountMl: Int) {
        addWater(amountMl)
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            val dateIso = DateUtils.toIsoDate(_selectedDate.value)
            waterRepository.logWater(dateIso, amountMl)
            lastAddedWaterAmount = amountMl
            _snackbarMessage.value = "Added $amountMl ml water"
        }
    }

    fun undoWaterAdd() {
        val amount = lastAddedWaterAmount ?: return
        viewModelScope.launch {
            val dateIso = DateUtils.toIsoDate(_selectedDate.value)
            waterRepository.logWater(dateIso, -amount)
            lastAddedWaterAmount = null
            _snackbarMessage.value = "Removed $amount ml water"
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun refresh() {
        onRefresh()
    }

    fun onRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Trigger flow emissions update
            val current = _selectedDate.value
            _selectedDate.value = current
            kotlinx.coroutines.delay(300)
            _isRefreshing.value = false
        }
    }
}
