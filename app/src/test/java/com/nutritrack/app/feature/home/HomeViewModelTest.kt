package com.nutritrack.app.feature.home

import com.nutritrack.app.core.datastore.NutriTrackPreferences
import com.nutritrack.app.domain.model.ActivityLevel
import com.nutritrack.app.domain.model.DiaryEntry
import com.nutritrack.app.domain.model.Gender
import com.nutritrack.app.domain.model.GoalType
import com.nutritrack.app.domain.model.MealType
import com.nutritrack.app.domain.model.StepDaySummary
import com.nutritrack.app.domain.model.UserProfile
import com.nutritrack.app.domain.repository.DiaryRepository
import com.nutritrack.app.domain.repository.StepRepository
import com.nutritrack.app.domain.repository.UserRepository
import com.nutritrack.app.domain.repository.WaterRepository
import com.nutritrack.app.domain.repository.WorkoutRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val userRepository: UserRepository = mockk(relaxed = true)
    private val diaryRepository: DiaryRepository = mockk(relaxed = true)
    private val waterRepository: WaterRepository = mockk(relaxed = true)
    private val stepRepository: StepRepository = mockk(relaxed = true)
    private val workoutRepository: WorkoutRepository = mockk(relaxed = true)
    private val preferences: NutriTrackPreferences = mockk(relaxed = true)

    private val sampleProfile = UserProfile(
        name = "Ahmad",
        gender = Gender.MALE,
        age = 25,
        heightCm = 175.0,
        weightKg = 70.0,
        activityLevel = ActivityLevel.MODERATELY_ACTIVE,
        goal = GoalType.LOSE_WEIGHT,
        calorieGoal = 2000,
        proteinGoalG = 150,
        carbsGoalG = 200,
        fatGoalG = 65,
        waterGoalMl = 2500,
        stepGoal = 10000
    )

    private val sampleEntry = DiaryEntry(
        id = "entry-1",
        date = LocalDate.now().toString(),
        meal = MealType.LUNCH,
        foodId = "food-1",
        nameSnapshot = "Biryani",
        emojiSnapshot = "🍛",
        servingLabelSnapshot = "1 plate",
        quantity = 1.0,
        calories = 500.0,
        protein = 30.0,
        carbs = 70.0,
        fat = 15.0,
        createdAt = System.currentTimeMillis()
    )

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { userRepository.getUserProfile() } returns flowOf(sampleProfile)
        every { diaryRepository.getEntriesForDate(any()) } returns flowOf(listOf(sampleEntry))
        every { diaryRepository.getAllLoggedDates() } returns flowOf(listOf(LocalDate.now().toString()))
        every { diaryRepository.getEntriesBetweenDates(any(), any()) } returns flowOf(listOf(sampleEntry))
        every { waterRepository.getTotalWaterForDate(any()) } returns flowOf(750)
        every { stepRepository.getStepDay(any()) } returns flowOf(StepDaySummary(date = LocalDate.now().toString(), sensorSteps = 4000))
        every { workoutRepository.getTotalWorkoutCalories(any()) } returns flowOf(200.0)

        viewModel = HomeViewModel(
            userRepository = userRepository,
            diaryRepository = diaryRepository,
            waterRepository = waterRepository,
            stepRepository = stepRepository,
            workoutRepository = workoutRepository,
            preferences = preferences
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState combines flows and calculates eaten and goals correctly`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals("Ahmad", state.userName)
        assertEquals(2000, state.calorieGoal)
        assertEquals(500, state.eatenCalories)
        assertEquals(30, state.proteinEatenG)
        assertEquals(70, state.carbsEatenG)
        assertEquals(15, state.fatEatenG)
        assertEquals(750, state.waterIntakeMl)
        assertEquals(4000, state.stepCount)
        assertEquals(500, state.lunchSummary.calories)
        assertEquals(1, state.lunchSummary.itemCount)
    }

    @Test
    fun `date navigation updates selected date`() = runTest {
        advanceUntilIdle()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        viewModel.onDateSelected(yesterday)
        assertEquals(yesterday, viewModel.selectedDate.value)

        viewModel.onTodayClick()
        assertEquals(today, viewModel.selectedDate.value)
    }

    @Test
    fun `addWater calls waterRepository with correct parameters`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
        viewModel.addWater(250)
        advanceUntilIdle()

        coVerify { waterRepository.logWater(any(), 250) }
        assertEquals("Added 250 ml water", viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `undoWaterAdd removes the last added water entry`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
        viewModel.addWater(500)
        advanceUntilIdle()

        viewModel.undoWaterAdd()
        advanceUntilIdle()

        coVerify { waterRepository.logWater(any(), -500) }
        assertEquals("Removed 500 ml water", viewModel.uiState.value.snackbarMessage)
    }
}
