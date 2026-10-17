package com.nutritrack.app.feature.food

import com.nutritrack.app.domain.model.FoodItem
import com.nutritrack.app.domain.model.FoodSource
import com.nutritrack.app.domain.model.MealType
import com.nutritrack.app.domain.repository.DiaryRepository
import com.nutritrack.app.domain.repository.FoodRepository
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
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddFoodViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val foodRepository: FoodRepository = mockk(relaxed = true)
    private val diaryRepository: DiaryRepository = mockk(relaxed = true)

    private val sampleFood = FoodItem(
        id = "f-1",
        name = "Chicken Biryani",
        category = "Desi",
        emoji = "🍛",
        servingLabel = "1 plate",
        calories = 490.0,
        protein = 28.0,
        carbs = 65.0,
        fat = 14.0,
        source = FoodSource.SEED,
        isFavorite = false
    )

    private lateinit var viewModel: AddFoodViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { foodRepository.getAllFoods() } returns flowOf(listOf(sampleFood))
        every { foodRepository.getRecentFoods(any()) } returns flowOf(listOf(sampleFood))
        every { foodRepository.getFavoriteFoods() } returns flowOf(listOf(sampleFood.copy(isFavorite = true)))
        every { foodRepository.getUserCustomFoods() } returns flowOf(listOf(sampleFood.copy(source = FoodSource.USER)))
        every { foodRepository.searchFoods(any()) } returns flowOf(listOf(sampleFood))

        viewModel = AddFoodViewModel(foodRepository, diaryRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState loads all foods and default meal`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals(AddFoodTab.ALL, state.activeTab)
        assertFalse(state.isSearching)
        assertEquals(1, state.foodResults.size)
        assertEquals("Chicken Biryani", state.foodResults[0].name)
    }

    @Test
    fun `search query debounces 200ms and executes search`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
        viewModel.onSearchQueryChanged("biryani")

        // Before debounce fires
        advanceTimeBy(100)
        coVerify(exactly = 0) { foodRepository.searchFoods("biryani") }

        // After 200ms debounce
        advanceTimeBy(150)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSearching)
        coVerify { foodRepository.searchFoods("biryani") }
    }

    @Test
    fun `tab switching requests corresponding food list`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.onTabSelected(AddFoodTab.FAVORITES)
        advanceUntilIdle()
        assertEquals(AddFoodTab.FAVORITES, viewModel.uiState.value.activeTab)
        coVerify { foodRepository.getFavoriteFoods() }

        viewModel.onTabSelected(AddFoodTab.RECENT)
        advanceUntilIdle()
        assertEquals(AddFoodTab.RECENT, viewModel.uiState.value.activeTab)
        coVerify { foodRepository.getRecentFoods(30) }

        viewModel.onTabSelected(AddFoodTab.MY_FOODS)
        advanceUntilIdle()
        assertEquals(AddFoodTab.MY_FOODS, viewModel.uiState.value.activeTab)
        coVerify { foodRepository.getUserCustomFoods() }
    }

    @Test
    fun `quickAddFood adds default serving to diary with undo capability`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
        viewModel.onMealSelected(MealType.LUNCH)
        viewModel.quickAddFood(sampleFood)
        advanceUntilIdle()

        coVerify { diaryRepository.addEntry(any()) }
        coVerify { foodRepository.markFoodUsed(sampleFood.id) }
        assertNotNull(viewModel.uiState.value.lastAddedEntry)
        assertEquals("Added to Lunch", viewModel.uiState.value.snackbarMessage)

        // Undo
        viewModel.undoQuickAdd()
        advanceUntilIdle()

        coVerify { diaryRepository.deleteEntry(any()) }
        assertNull(viewModel.uiState.value.lastAddedEntry)
    }

    @Test
    fun `addQuickCalories creates raw calorie entry without food item`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
        viewModel.onMealSelected(MealType.DINNER)
        viewModel.addQuickCalories("Snack", 200.0, 5.0, 30.0, 6.0)
        advanceUntilIdle()

        coVerify { diaryRepository.addEntry(match { it.isQuickAdd && it.calories == 200.0 }) }
        assertEquals("Added to Dinner", viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `toggleFavorite updates favorite state in repository`() = runTest {
        advanceUntilIdle()
        viewModel.toggleFavorite(sampleFood)
        advanceUntilIdle()

        coVerify { foodRepository.toggleFavorite(sampleFood.id, true) }
    }
}
