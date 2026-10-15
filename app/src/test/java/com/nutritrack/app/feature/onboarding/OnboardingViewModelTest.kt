package com.nutritrack.app.feature.onboarding

import com.nutritrack.app.core.datastore.NutriTrackPreferences
import com.nutritrack.app.domain.calc.CalorieGoalCalculator
import com.nutritrack.app.domain.model.ActivityLevel
import com.nutritrack.app.domain.model.Gender
import com.nutritrack.app.domain.model.GoalType
import com.nutritrack.app.domain.model.UserProfile
import com.nutritrack.app.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val preferences: NutriTrackPreferences = mockk(relaxed = true)

    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = OnboardingViewModel(userRepository, preferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has expected defaults and calculated targets`() {
        val state = viewModel.uiState.value

        assertEquals(OnboardingScreenRoute.WELCOME, state.currentRoute)
        assertEquals(WizardStep.STEP_1_NAME_GENDER, state.currentWizardStep)
        assertEquals(0, state.welcomeSlideIndex)
        assertFalse(state.isDisclaimerChecked)
        assertEquals(Gender.MALE, state.gender)
        assertEquals(25, state.age)
        assertEquals(175.0, state.heightCm, 0.01)
        assertEquals(75.0, state.weightKg, 0.01)
        assertEquals(ActivityLevel.MODERATELY_ACTIVE, state.activityLevel)
        assertEquals(GoalType.LOSE_WEIGHT, state.goalType)
        assertTrue(state.targetCalories > 0)
        assertNotNull(state.bmiResult)
        assertFalse(state.isComplete)
    }

    @Test
    fun `welcome carousel slide navigation advances and skips to disclaimer`() {
        assertEquals(0, viewModel.uiState.value.welcomeSlideIndex)

        viewModel.onWelcomeNextClicked()
        assertEquals(1, viewModel.uiState.value.welcomeSlideIndex)

        viewModel.onWelcomeNextClicked()
        assertEquals(2, viewModel.uiState.value.welcomeSlideIndex)

        // Beyond last slide -> moves to DISCLAIMER
        viewModel.onWelcomeNextClicked()
        assertEquals(OnboardingScreenRoute.DISCLAIMER, viewModel.uiState.value.currentRoute)

        // Or skipping immediately
        viewModel.onWelcomeSlideChanged(0)
        viewModel.onWelcomeSkipClicked()
        assertEquals(OnboardingScreenRoute.DISCLAIMER, viewModel.uiState.value.currentRoute)
    }

    @Test
    fun `disclaimer requires checkbox before proceeding to wizard`() {
        viewModel.onWelcomeSkipClicked()
        assertEquals(OnboardingScreenRoute.DISCLAIMER, viewModel.uiState.value.currentRoute)

        // Attempting to continue without checking
        viewModel.onDisclaimerContinueClicked()
        assertEquals(OnboardingScreenRoute.DISCLAIMER, viewModel.uiState.value.currentRoute)

        // Check the box
        viewModel.onDisclaimerCheckedChanged(true)
        assertTrue(viewModel.uiState.value.isDisclaimerChecked)

        viewModel.onDisclaimerContinueClicked()
        assertEquals(OnboardingScreenRoute.WIZARD, viewModel.uiState.value.currentRoute)
        assertEquals(WizardStep.STEP_1_NAME_GENDER, viewModel.uiState.value.currentWizardStep)
    }

    @Test
    fun `step 1 name and gender update recalculates MSJ targets`() {
        viewModel.onNameChanged("Ali")
        assertEquals("Ali", viewModel.uiState.value.name)

        val maleCalories = viewModel.uiState.value.targetCalories

        // Switch to female (MSJ female has -161 instead of +5)
        viewModel.onGenderChanged(Gender.FEMALE)
        assertEquals(Gender.FEMALE, viewModel.uiState.value.gender)
        val femaleCalories = viewModel.uiState.value.targetCalories

        assertTrue("Female calorie target should be lower than male target due to MSJ formula", femaleCalories < maleCalories)

        // Advance to Step 2
        viewModel.onWizardNextClicked()
        assertEquals(WizardStep.STEP_2_AGE, viewModel.uiState.value.currentWizardStep)
    }

    @Test
    fun `step 2 age bounds validation and increment decrement`() {
        // Move to step 2
        viewModel.onWizardNextClicked()
        assertEquals(WizardStep.STEP_2_AGE, viewModel.uiState.value.currentWizardStep)

        // Age increment and decrement
        val initialAge = viewModel.uiState.value.age
        viewModel.incrementAge()
        assertEquals(initialAge + 1, viewModel.uiState.value.age)

        viewModel.decrementAge()
        assertEquals(initialAge, viewModel.uiState.value.age)

        // Test out of bounds: < 14
        viewModel.onAgeChanged("10")
        assertNotNull(viewModel.uiState.value.ageError)
        viewModel.onWizardNextClicked()
        assertEquals(WizardStep.STEP_2_AGE, viewModel.uiState.value.currentWizardStep) // Blocked

        // Test out of bounds: > 100
        viewModel.onAgeChanged("105")
        assertNotNull(viewModel.uiState.value.ageError)
        viewModel.onWizardNextClicked()
        assertEquals(WizardStep.STEP_2_AGE, viewModel.uiState.value.currentWizardStep) // Blocked

        // Valid age: 30
        viewModel.onAgeChanged("30")
        assertNull(viewModel.uiState.value.ageError)
        viewModel.onWizardNextClicked()
        assertEquals(WizardStep.STEP_3_HEIGHT_WEIGHT, viewModel.uiState.value.currentWizardStep)
    }

    @Test
    fun `step 3 unit toggle converts values between metric and imperial`() {
        // Step 1 -> Step 2 -> Step 3
        viewModel.onWizardNextClicked() // to Step 2
        viewModel.onWizardNextClicked() // to Step 3
        assertEquals(WizardStep.STEP_3_HEIGHT_WEIGHT, viewModel.uiState.value.currentWizardStep)

        assertEquals(UnitSystem.METRIC, viewModel.uiState.value.unitSystem)
        val kg = viewModel.uiState.value.weightKg
        val cm = viewModel.uiState.value.heightCm

        // Toggle to Imperial
        viewModel.onUnitSystemToggled(UnitSystem.IMPERIAL)
        assertEquals(UnitSystem.IMPERIAL, viewModel.uiState.value.unitSystem)
        val expectedLbs = kg * 2.20462
        val actualLbs = viewModel.uiState.value.weightInputText.toDouble()
        assertEquals(expectedLbs, actualLbs, 0.5)

        // Toggle back to Metric
        viewModel.onUnitSystemToggled(UnitSystem.METRIC)
        assertEquals(UnitSystem.METRIC, viewModel.uiState.value.unitSystem)
        assertEquals(kg, viewModel.uiState.value.weightKg, 0.1)
        assertEquals(cm, viewModel.uiState.value.heightCm, 0.1)

        // Live BMI updates
        val bmi = viewModel.uiState.value.bmiResult
        assertNotNull(bmi)
        assertTrue(bmi!!.bmi in 15.0..45.0)

        // Advance to Step 4
        viewModel.onWizardNextClicked()
        assertEquals(WizardStep.STEP_4_ACTIVITY, viewModel.uiState.value.currentWizardStep)
    }

    @Test
    fun `step 4 activity level scales TDEE correctly`() {
        // Move to step 4
        viewModel.onWizardNextClicked() // to Step 2
        viewModel.onWizardNextClicked() // to Step 3
        viewModel.onWizardNextClicked() // to Step 4

        viewModel.onActivityLevelChanged(ActivityLevel.SEDENTARY)
        val sedentaryTdee = viewModel.uiState.value.tdee

        viewModel.onActivityLevelChanged(ActivityLevel.VERY_ACTIVE)
        val veryActiveTdee = viewModel.uiState.value.tdee

        assertTrue("Very active TDEE should be significantly higher than sedentary TDEE", veryActiveTdee > sedentaryTdee)

        viewModel.onWizardNextClicked()
        assertEquals(WizardStep.STEP_5_GOAL_PACE, viewModel.uiState.value.currentWizardStep)
    }

    @Test
    fun `step 5 goal and pace navigation advances to plan reveal`() {
        // Move to step 5
        viewModel.onWizardNextClicked() // 2
        viewModel.onWizardNextClicked() // 3
        viewModel.onWizardNextClicked() // 4
        viewModel.onWizardNextClicked() // 5

        viewModel.onGoalTypeChanged(GoalType.LOSE_WEIGHT)
        viewModel.onPaceChanged(0.75)
        assertEquals(0.75, viewModel.uiState.value.paceKgPerWeek, 0.01)

        viewModel.onWizardNextClicked()
        assertEquals(OnboardingScreenRoute.PLAN_REVEAL, viewModel.uiState.value.currentRoute)
        assertTrue(viewModel.uiState.value.targetCalories > 0)
    }

    @Test
    fun `back button traverses steps in reverse order`() {
        // Welcome -> Disclaimer -> Step 1 -> Step 2
        viewModel.onWelcomeSkipClicked()
        viewModel.onDisclaimerCheckedChanged(true)
        viewModel.onDisclaimerContinueClicked()
        viewModel.onWizardNextClicked() // At Step 2

        assertEquals(WizardStep.STEP_2_AGE, viewModel.uiState.value.currentWizardStep)

        viewModel.onWizardBackClicked()
        assertEquals(WizardStep.STEP_1_NAME_GENDER, viewModel.uiState.value.currentWizardStep)

        viewModel.onWizardBackClicked()
        assertEquals(OnboardingScreenRoute.DISCLAIMER, viewModel.uiState.value.currentRoute)

        viewModel.onWizardBackClicked()
        assertEquals(OnboardingScreenRoute.WELCOME, viewModel.uiState.value.currentRoute)
    }

    @Test
    fun `fine tuning slider adjusts calories and recalculates macros rounded to 10 kcal`() {
        viewModel.onSliderCalorieChanged(2143.7f)

        // Should round to 2140 kcal
        assertEquals(2140, viewModel.uiState.value.targetCalories)
        val macros = viewModel.uiState.value.macroSplit
        assertTrue(macros.proteinGrams > 0)
        assertTrue(macros.carbsGrams > 0)
        assertTrue(macros.fatGrams > 0)

        // Sum of kcal from macros should be approximately 2140
        val macroKcal = (macros.proteinGrams * 4) + (macros.carbsGrams * 4) + (macros.fatGrams * 9)
        assertEquals(2140.0, macroKcal.toDouble(), 40.0)
    }

    @Test
    fun `completeOnboarding saves profile and updates preferences`() = runTest {
        viewModel.onNameChanged("Ahmad")
        viewModel.onAgeChanged("28")
        viewModel.onHeightCmChanged("180")
        viewModel.onWeightChanged("80")
        viewModel.onActivityLevelChanged(ActivityLevel.MODERATELY_ACTIVE)
        viewModel.onGoalTypeChanged(GoalType.LOSE_WEIGHT)
        viewModel.onPaceChanged(0.5)

        val profileSlot = slot<UserProfile>()
        coEvery { userRepository.saveUserProfile(capture(profileSlot)) } returns Unit
        coEvery { preferences.setDailyCalorieGoal(any()) } returns Unit
        coEvery { preferences.setDailyWaterGoalMl(any()) } returns Unit
        coEvery { preferences.setDailyStepGoal(any()) } returns Unit
        coEvery { preferences.setWeightUnit(any()) } returns Unit
        coEvery { preferences.setHeightUnit(any()) } returns Unit
        coEvery { preferences.setOnboardingCompleted(true) } returns Unit

        viewModel.completeOnboarding()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isComplete)
        coVerify(exactly = 1) { userRepository.saveUserProfile(any()) }
        coVerify(exactly = 1) { preferences.setOnboardingCompleted(true) }

        val saved = profileSlot.captured
        assertEquals("Ahmad", saved.name)
        assertEquals(28, saved.age)
        assertEquals(180.0, saved.heightCm, 0.1)
        assertEquals(80.0, saved.weightKg, 0.1)
        assertEquals(ActivityLevel.MODERATELY_ACTIVE, saved.activityLevel)
        assertEquals(GoalType.LOSE_WEIGHT, saved.goal)
        assertTrue(saved.calorieGoal > 0)
    }
}
