package com.nutritrack.app.feature.food

import com.nutritrack.app.domain.model.DiaryEntry
import com.nutritrack.app.domain.model.FoodItem
import com.nutritrack.app.domain.model.FoodSource
import com.nutritrack.app.domain.model.MealType
import com.nutritrack.app.domain.model.ServingOption
import com.nutritrack.app.domain.repository.DiaryRepository
import com.nutritrack.app.domain.repository.FoodRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
class FoodDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val foodRepository: FoodRepository = mockk(relaxed = true)
    private val diaryRepository: DiaryRepository = mockk(relaxed = true)

    private val sampleFood = FoodItem(
        id = "f-100",
        name = "Chapati / Roti",
        category = "Desi",
        emoji = "🫓",
        servingLabel = "1 piece (40g)",
        calories = 104.0,
        protein = 3.1,
        carbs = 20.4,
        fat = 0.5,
        source = FoodSource.SEED,
        servingOptions = listOf(
            ServingOption(id = "opt-1", foodId = "f-100", label = "100g", multiplier = 2.5)
        )
    )

    private lateinit var viewModel: FoodDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { foodRepository.getFoodByIdSync("f-100") } returns sampleFood

        viewModel = FoodDetailViewModel(foodRepository, diaryRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadFood computes correct nutrition for base serving`() = runTest {
        viewModel.loadFood("f-100", MealType.LUNCH, LocalDate.now())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Chapati / Roti", state.food?.name)
        assertEquals(2, state.servingOptions.size) // base + 1 alternate
        assertEquals(1.0f, state.quantity)
        assertEquals(104.0, state.computedNutrition.calories, 0.01)
        assertEquals(3.1, state.computedNutrition.protein, 0.01)
        assertEquals(20.4, state.computedNutrition.carbs, 0.01)
        assertEquals(0.5, state.computedNutrition.fat, 0.01)
    }

    @Test
    fun `quantity change scales nutrients proportionally`() = runTest {
        viewModel.loadFood("f-100", MealType.LUNCH, LocalDate.now())
        advanceUntilIdle()

        viewModel.onQuantityChanged(2.5f)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2.5f, state.quantity)
        assertEquals(104.0 * 2.5, state.computedNutrition.calories, 0.01)
        assertEquals(3.1 * 2.5, state.computedNutrition.protein, 0.01)
    }

    @Test
    fun `serving option change scales nutrients with multiplier`() = runTest {
        viewModel.loadFood("f-100", MealType.LUNCH, LocalDate.now())
        advanceUntilIdle()

        val alternateOption = viewModel.uiState.value.servingOptions.first { it.id == "opt-1" }
        viewModel.onServingSelected(alternateOption)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("100g", state.selectedServingOption?.label)
        assertEquals(104.0 * 2.5, state.computedNutrition.calories, 0.01)
    }

    @Test
    fun `saveToDiary saves entry and triggers isSaved state`() = runTest {
        viewModel.loadFood("f-100", MealType.DINNER, LocalDate.now())
        advanceUntilIdle()

        viewModel.onQuantityChanged(2.0f)
        advanceUntilIdle()

        viewModel.saveToDiary()
        advanceUntilIdle()

        coVerify { diaryRepository.addEntry(match { it.meal == MealType.DINNER && it.calories == 208.0 }) }
        coVerify { foodRepository.markFoodUsed("f-100") }
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `loadForEditing populates existing entry and updates on save`() = runTest {
        val entry = DiaryEntry(
            id = "entry-existing",
            date = "2026-10-16",
            meal = MealType.BREAKFAST,
            foodId = "f-100",
            nameSnapshot = "Chapati / Roti",
            emojiSnapshot = "🫓",
            servingLabelSnapshot = "1 piece (40g)",
            quantity = 1.5,
            calories = 156.0,
            protein = 4.65,
            carbs = 30.6,
            fat = 0.75,
            createdAt = System.currentTimeMillis()
        )

        viewModel.loadForEditing(entry)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEditingExistingEntry)
        assertEquals("entry-existing", state.editingEntryId)
        assertEquals(1.5f, state.quantity)
        assertEquals(MealType.BREAKFAST, state.selectedMeal)

        // Modify quantity and update
        viewModel.onQuantityChanged(3.0f)
        advanceUntilIdle()

        viewModel.saveToDiary()
        advanceUntilIdle()

        coVerify { diaryRepository.updateEntry(match { it.id == "entry-existing" && it.quantity == 3.0 }) }
        assertTrue(viewModel.uiState.value.isSaved)
    }
}
