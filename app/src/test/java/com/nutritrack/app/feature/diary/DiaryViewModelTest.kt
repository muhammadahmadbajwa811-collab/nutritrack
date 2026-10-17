package com.nutritrack.app.feature.diary

import com.nutritrack.app.domain.model.ActivityLevel
import com.nutritrack.app.domain.model.DiaryEntry
import com.nutritrack.app.domain.model.Gender
import com.nutritrack.app.domain.model.GoalType
import com.nutritrack.app.domain.model.MealType
import com.nutritrack.app.domain.model.UserProfile
import com.nutritrack.app.domain.repository.DiaryRepository
import com.nutritrack.app.domain.repository.UserRepository
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
class DiaryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val diaryRepository: DiaryRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)

    private val sampleProfile = UserProfile(
        name = "Ahmad",
        gender = Gender.MALE,
        age = 25,
        heightCm = 175.0,
        weightKg = 70.0,
        activityLevel = ActivityLevel.MODERATELY_ACTIVE,
        goal = GoalType.LOSE_WEIGHT,
        calorieGoal = 2200,
        proteinGoalG = 140,
        carbsGoalG = 240,
        fatGoalG = 60
    )

    private val breakfastEntry = DiaryEntry(
        id = "b-1",
        date = LocalDate.now().toString(),
        meal = MealType.BREAKFAST,
        foodId = "f-1",
        nameSnapshot = "Egg",
        emojiSnapshot = "🥚",
        servingLabelSnapshot = "1 piece",
        quantity = 2.0,
        calories = 150.0,
        protein = 12.0,
        carbs = 2.0,
        fat = 10.0,
        createdAt = System.currentTimeMillis()
    )

    private val lunchEntry = DiaryEntry(
        id = "l-1",
        date = LocalDate.now().toString(),
        meal = MealType.LUNCH,
        foodId = "f-2",
        nameSnapshot = "Roti & Daal",
        emojiSnapshot = "🍲",
        servingLabelSnapshot = "1 bowl",
        quantity = 1.0,
        calories = 350.0,
        protein = 14.0,
        carbs = 50.0,
        fat = 8.0,
        createdAt = System.currentTimeMillis()
    )

    private lateinit var viewModel: DiaryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { userRepository.getUserProfile() } returns flowOf(sampleProfile)
        every { diaryRepository.getEntriesForDate(any()) } returns flowOf(listOf(breakfastEntry, lunchEntry))

        viewModel = DiaryViewModel(
            diaryRepository = diaryRepository,
            userRepository = userRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState computes total calories and groups entries by meal`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals(500, state.totalCalories) // 150 + 350
        assertEquals(26, state.totalProtein)  // 12 + 14
        assertEquals(52, state.totalCarbs)    // 2 + 50
        assertEquals(18, state.totalFat)      // 10 + 8
        assertEquals(2200, state.calorieGoal)

        val breakfastList = state.entriesByMeal[MealType.BREAKFAST].orEmpty()
        assertEquals(1, breakfastList.size)
        assertEquals("Egg", breakfastList[0].nameSnapshot)

        val lunchList = state.entriesByMeal[MealType.LUNCH].orEmpty()
        assertEquals(1, lunchList.size)
        assertEquals("Roti & Daal", lunchList[0].nameSnapshot)
    }

    @Test
    fun `date navigation respects past and today boundaries`() = runTest {
        advanceUntilIdle()
        val today = LocalDate.now()

        viewModel.onPreviousDay()
        assertEquals(today.minusDays(1), viewModel.selectedDate.value)

        viewModel.onNextDay()
        assertEquals(today, viewModel.selectedDate.value)

        // Cannot navigate into the future
        viewModel.onNextDay()
        assertEquals(today, viewModel.selectedDate.value)
    }

    @Test
    fun `toggleMealCollapse toggles meal collapse state`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isMealCollapsed(MealType.BREAKFAST))

        viewModel.toggleMealCollapse(MealType.BREAKFAST)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isMealCollapsed(MealType.BREAKFAST))

        viewModel.toggleMealCollapse(MealType.BREAKFAST)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isMealCollapsed(MealType.BREAKFAST))
    }

    @Test
    fun `deleteEntry removes entry and undoDelete restores it`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
        viewModel.deleteEntry(breakfastEntry)
        advanceUntilIdle()

        coVerify { diaryRepository.deleteEntry("b-1") }
        assertEquals(breakfastEntry, viewModel.uiState.value.recentlyDeletedEntry)

        viewModel.undoDelete()
        advanceUntilIdle()

        coVerify { diaryRepository.addEntry(breakfastEntry) }
    }

    @Test
    fun `clearDay and undoClearDay work as expected`() = runTest {
        advanceUntilIdle()
        coEvery { diaryRepository.getEntriesForDateSync(any()) } returns listOf(breakfastEntry, lunchEntry)

        viewModel.clearDay()
        advanceUntilIdle()

        coVerify { diaryRepository.clearDate(any()) }

        viewModel.undoClearDay()
        advanceUntilIdle()

        coVerify { diaryRepository.addEntries(listOf(breakfastEntry, lunchEntry)) }
    }

    @Test
    fun `copyFromYesterday copies entries from previous day`() = runTest {
        advanceUntilIdle()
        coEvery { diaryRepository.getEntriesForDateSync(any()) } returns listOf(breakfastEntry)

        viewModel.copyFromYesterday()
        advanceUntilIdle()

        coVerify { diaryRepository.addEntries(any()) }
    }
}
