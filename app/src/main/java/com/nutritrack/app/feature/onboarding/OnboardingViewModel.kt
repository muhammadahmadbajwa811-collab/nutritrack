package com.nutritrack.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutritrack.app.core.datastore.NutriTrackPreferences
import com.nutritrack.app.domain.calc.BmiCalculator
import com.nutritrack.app.domain.calc.BmrCalculator
import com.nutritrack.app.domain.calc.CalorieGoalCalculator
import com.nutritrack.app.domain.calc.MacroCalculator
import com.nutritrack.app.domain.calc.StepCalculator
import com.nutritrack.app.domain.calc.TdeeCalculator
import com.nutritrack.app.domain.calc.WaterGoalCalculator
import com.nutritrack.app.domain.model.ActivityLevel
import com.nutritrack.app.domain.model.Gender
import com.nutritrack.app.domain.model.GoalType
import com.nutritrack.app.domain.model.MacroPreset
import com.nutritrack.app.domain.model.UserProfile
import com.nutritrack.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Year
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferences: NutriTrackPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        recalculateTargets()
    }

    // --- Navigation ---

    fun onWelcomeSlideChanged(index: Int) {
        _uiState.update { it.copy(welcomeSlideIndex = index) }
    }

    fun onWelcomeNextClicked() {
        val current = _uiState.value.welcomeSlideIndex
        if (current < 2) {
            _uiState.update { it.copy(welcomeSlideIndex = current + 1) }
        } else {
            _uiState.update { it.copy(currentRoute = OnboardingScreenRoute.DISCLAIMER) }
        }
    }

    fun onWelcomeSkipClicked() {
        _uiState.update { it.copy(currentRoute = OnboardingScreenRoute.DISCLAIMER) }
    }

    fun onDisclaimerCheckedChanged(checked: Boolean) {
        _uiState.update { it.copy(isDisclaimerChecked = checked) }
    }

    fun onDisclaimerContinueClicked() {
        if (_uiState.value.isDisclaimerChecked) {
            _uiState.update {
                it.copy(
                    currentRoute = OnboardingScreenRoute.WIZARD,
                    currentWizardStep = WizardStep.STEP_1_NAME_GENDER
                )
            }
        }
    }

    fun onWizardBackClicked() {
        val state = _uiState.value
        when (state.currentRoute) {
            OnboardingScreenRoute.PLAN_REVEAL -> {
                _uiState.update {
                    it.copy(
                        currentRoute = OnboardingScreenRoute.WIZARD,
                        currentWizardStep = WizardStep.STEP_5_GOAL_PACE
                    )
                }
            }
            OnboardingScreenRoute.WIZARD -> {
                when (state.currentWizardStep) {
                    WizardStep.STEP_1_NAME_GENDER -> {
                        _uiState.update { it.copy(currentRoute = OnboardingScreenRoute.DISCLAIMER) }
                    }
                    WizardStep.STEP_2_AGE -> {
                        _uiState.update { it.copy(currentWizardStep = WizardStep.STEP_1_NAME_GENDER) }
                    }
                    WizardStep.STEP_3_HEIGHT_WEIGHT -> {
                        _uiState.update { it.copy(currentWizardStep = WizardStep.STEP_2_AGE) }
                    }
                    WizardStep.STEP_4_ACTIVITY -> {
                        _uiState.update { it.copy(currentWizardStep = WizardStep.STEP_3_HEIGHT_WEIGHT) }
                    }
                    WizardStep.STEP_5_GOAL_PACE -> {
                        _uiState.update { it.copy(currentWizardStep = WizardStep.STEP_4_ACTIVITY) }
                    }
                }
            }
            OnboardingScreenRoute.DISCLAIMER -> {
                _uiState.update { it.copy(currentRoute = OnboardingScreenRoute.WELCOME) }
            }
            OnboardingScreenRoute.WELCOME -> {
                // At root welcome
            }
        }
    }

    fun onWizardNextClicked() {
        val state = _uiState.value
        when (state.currentWizardStep) {
            WizardStep.STEP_1_NAME_GENDER -> {
                _uiState.update { it.copy(currentWizardStep = WizardStep.STEP_2_AGE) }
            }
            WizardStep.STEP_2_AGE -> {
                if (validateAge()) {
                    _uiState.update { it.copy(currentWizardStep = WizardStep.STEP_3_HEIGHT_WEIGHT) }
                }
            }
            WizardStep.STEP_3_HEIGHT_WEIGHT -> {
                if (validateHeightAndWeight()) {
                    _uiState.update { it.copy(currentWizardStep = WizardStep.STEP_4_ACTIVITY) }
                }
            }
            WizardStep.STEP_4_ACTIVITY -> {
                _uiState.update { it.copy(currentWizardStep = WizardStep.STEP_5_GOAL_PACE) }
            }
            WizardStep.STEP_5_GOAL_PACE -> {
                if (validateGoal()) {
                    recalculateTargets()
                    _uiState.update { it.copy(currentRoute = OnboardingScreenRoute.PLAN_REVEAL) }
                }
            }
        }
    }

    // --- Inputs: Step 1 Name & Gender ---

    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onGenderChanged(newGender: Gender) {
        _uiState.update { it.copy(gender = newGender) }
        recalculateTargets()
    }

    // --- Inputs: Step 2 Age ---

    fun onAgeChanged(ageText: String) {
        val parsedAge = ageText.toIntOrNull()
        _uiState.update {
            it.copy(
                ageInputText = ageText,
                age = parsedAge ?: it.age,
                ageError = if (parsedAge != null && (parsedAge < 14 || parsedAge > 100)) "Age must be between 14 and 100" else null
            )
        }
        if (parsedAge != null && parsedAge in 14..100) {
            recalculateTargets()
        }
    }

    fun incrementAge() {
        val newAge = minOf(100, _uiState.value.age + 1)
        _uiState.update { it.copy(age = newAge, ageInputText = newAge.toString(), ageError = null) }
        recalculateTargets()
    }

    fun decrementAge() {
        val newAge = maxOf(14, _uiState.value.age - 1)
        _uiState.update { it.copy(age = newAge, ageInputText = newAge.toString(), ageError = null) }
        recalculateTargets()
    }

    private fun validateAge(): Boolean {
        val age = _uiState.value.age
        val valid = age in 14..100
        _uiState.update {
            it.copy(ageError = if (valid) null else "Please enter a valid age between 14 and 100")
        }
        return valid
    }

    // --- Inputs: Step 3 Height & Weight ---

    fun onUnitSystemToggled(newUnit: UnitSystem) {
        val state = _uiState.value
        if (state.unitSystem == newUnit) return

        if (newUnit == UnitSystem.IMPERIAL) {
            // Metric -> Imperial
            val lbs = state.weightKg * 2.20462
            val totalInches = (state.heightCm / 2.54).roundToInt()
            val feet = totalInches / 12
            val inches = totalInches % 12
            val goalLbs = state.goalWeightKg * 2.20462

            _uiState.update {
                it.copy(
                    unitSystem = UnitSystem.IMPERIAL,
                    weightInputText = "%.1f".format(lbs),
                    heightFeetInputText = feet.toString(),
                    heightInchesInputText = inches.toString(),
                    goalWeightInputText = "%.1f".format(goalLbs)
                )
            }
        } else {
            // Imperial -> Metric
            _uiState.update {
                it.copy(
                    unitSystem = UnitSystem.METRIC,
                    weightInputText = "%.1f".format(state.weightKg),
                    heightInputText = state.heightCm.roundToInt().toString(),
                    goalWeightInputText = "%.1f".format(state.goalWeightKg)
                )
            }
        }
    }

    fun onHeightCmChanged(text: String) {
        val parsed = text.toDoubleOrNull()
        _uiState.update {
            it.copy(
                heightInputText = text,
                heightCm = parsed ?: it.heightCm,
                heightError = if (parsed != null && (parsed < 100.0 || parsed > 250.0)) "Height must be between 100 and 250 cm" else null
            )
        }
        if (parsed != null && parsed in 100.0..250.0) recalculateTargets()
    }

    fun onHeightFeetChanged(feetText: String) {
        _uiState.update { it.copy(heightFeetInputText = feetText) }
        updateHeightFromFeetInches()
    }

    fun onHeightInchesChanged(inchesText: String) {
        _uiState.update { it.copy(heightInchesInputText = inchesText) }
        updateHeightFromFeetInches()
    }

    private fun updateHeightFromFeetInches() {
        val feet = _uiState.value.heightFeetInputText.toIntOrNull() ?: 0
        val inches = _uiState.value.heightInchesInputText.toIntOrNull() ?: 0
        val totalInches = (feet * 12) + inches
        if (totalInches > 0) {
            val cm = totalInches * 2.54
            _uiState.update { it.copy(heightCm = cm, heightError = null) }
            recalculateTargets()
        }
    }

    fun onWeightChanged(text: String) {
        val parsed = text.toDoubleOrNull()
        if (parsed != null) {
            val kg = if (_uiState.value.unitSystem == UnitSystem.IMPERIAL) parsed / 2.20462 else parsed
            _uiState.update {
                it.copy(
                    weightInputText = text,
                    weightKg = kg,
                    weightError = if (kg < 30.0 || kg > 300.0) "Weight must be between 30 and 300 kg" else null
                )
            }
            if (kg in 30.0..300.0) recalculateTargets()
        } else {
            _uiState.update { it.copy(weightInputText = text) }
        }
    }

    private fun validateHeightAndWeight(): Boolean {
        val state = _uiState.value
        val heightValid = state.heightCm in 100.0..250.0
        val weightValid = state.weightKg in 30.0..300.0

        _uiState.update {
            it.copy(
                heightError = if (heightValid) null else "Enter a valid height",
                weightError = if (weightValid) null else "Enter a valid weight"
            )
        }
        return heightValid && weightValid
    }

    // --- Inputs: Step 4 Activity Level ---

    fun onActivityLevelChanged(level: ActivityLevel) {
        _uiState.update { it.copy(activityLevel = level) }
        recalculateTargets()
    }

    // --- Inputs: Step 5 Goal & Pace ---

    fun onGoalTypeChanged(goal: GoalType) {
        _uiState.update { it.copy(goalType = goal) }
        recalculateTargets()
    }

    fun onGoalWeightChanged(text: String) {
        val parsed = text.toDoubleOrNull()
        if (parsed != null) {
            val kg = if (_uiState.value.unitSystem == UnitSystem.IMPERIAL) parsed / 2.20462 else parsed
            _uiState.update {
                it.copy(
                    goalWeightInputText = text,
                    goalWeightKg = kg,
                    goalWeightError = null
                )
            }
            recalculateTargets()
        } else {
            _uiState.update { it.copy(goalWeightInputText = text) }
        }
    }

    fun onPaceChanged(paceKg: Double) {
        _uiState.update { it.copy(paceKgPerWeek = paceKg) }
        recalculateTargets()
    }

    private fun validateGoal(): Boolean {
        val state = _uiState.value
        if (state.goalType == GoalType.MAINTAIN_WEIGHT) return true

        val valid = state.goalWeightKg in 30.0..300.0
        _uiState.update {
            it.copy(goalWeightError = if (valid) null else "Enter a valid goal weight")
        }
        return valid
    }

    // --- Plan Reveal: Fine-Tuning Slider ---

    fun onSliderCalorieChanged(calories: Float) {
        val roundedKcal = (calories / 10).roundToInt() * 10
        val macros = MacroCalculator.calculateMacroSplit(roundedKcal, _uiState.value.macroPreset)
        _uiState.update {
            it.copy(
                sliderCalorieValue = calories,
                targetCalories = roundedKcal,
                macroSplit = macros
            )
        }
    }

    // --- Target Calculation Engine ---

    private fun recalculateTargets() {
        val state = _uiState.value
        val bmr = BmrCalculator.calculateBmr(state.weightKg, state.heightCm, state.age, state.gender)
        val tdee = TdeeCalculator.calculateTdee(bmr, state.activityLevel)
        val goalResult = CalorieGoalCalculator.calculateTarget(
            tdee = tdee,
            goalType = state.goalType,
            paceKgPerWeek = state.paceKgPerWeek,
            gender = state.gender
        )

        val macros = MacroCalculator.calculateMacroSplit(goalResult.targetCalories, state.macroPreset)
        val waterGoal = WaterGoalCalculator.calculateDailyWaterGoalMl(state.weightKg)
        val stepGoal = StepCalculator.getDefaultStepGoal(state.activityLevel)
        val bmi = BmiCalculator.getBmiResult(state.weightKg, state.heightCm)

        _uiState.update {
            it.copy(
                tdee = tdee,
                calorieGoalResult = goalResult,
                targetCalories = goalResult.targetCalories,
                sliderCalorieValue = goalResult.targetCalories.toFloat(),
                macroSplit = macros,
                waterGoalMl = waterGoal,
                stepGoal = stepGoal,
                bmiResult = bmi
            )
        }
    }

    // --- Completion ---

    fun completeOnboarding() {
        val state = _uiState.value
        val currentYear = Year.now().value
        val birthYear = currentYear - state.age

        val userProfile = UserProfile(
            name = state.name.ifBlank { "User" },
            gender = state.gender,
            birthYear = birthYear,
            age = state.age,
            heightCm = state.heightCm,
            weightKg = state.weightKg,
            goalWeightKg = state.goalWeightKg,
            activityLevel = state.activityLevel,
            goal = state.goalType,
            paceKgPerWeek = state.paceKgPerWeek,
            calorieGoal = state.targetCalories,
            macroPreset = state.macroPreset,
            proteinGoalG = state.macroSplit.proteinGrams,
            carbsGoalG = state.macroSplit.carbsGrams,
            fatGoalG = state.macroSplit.fatGrams,
            waterGoalMl = state.waterGoalMl,
            stepGoal = state.stepGoal,
            customCalorieTarget = state.targetCalories
        )

        viewModelScope.launch {
            userRepository.saveUserProfile(userProfile)
            preferences.setDailyCalorieGoal(state.targetCalories)
            preferences.setDailyWaterGoalMl(state.waterGoalMl)
            preferences.setDailyStepGoal(state.stepGoal)
            preferences.setWeightUnit(if (state.unitSystem == UnitSystem.IMPERIAL) "LB" else "KG")
            preferences.setHeightUnit(if (state.unitSystem == UnitSystem.IMPERIAL) "FT_IN" else "CM")
            preferences.setOnboardingCompleted(true)
            _uiState.update { it.copy(isComplete = true) }
        }
    }
}
