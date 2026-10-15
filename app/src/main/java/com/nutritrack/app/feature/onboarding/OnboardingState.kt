package com.nutritrack.app.feature.onboarding

import com.nutritrack.app.domain.calc.BmiResult
import com.nutritrack.app.domain.calc.CalorieGoalResult
import com.nutritrack.app.domain.model.ActivityLevel
import com.nutritrack.app.domain.model.Gender
import com.nutritrack.app.domain.model.GoalType
import com.nutritrack.app.domain.model.MacroPreset
import com.nutritrack.app.domain.model.MacroSplit

enum class OnboardingScreenRoute {
    WELCOME,
    DISCLAIMER,
    WIZARD,
    PLAN_REVEAL
}

enum class WizardStep(val stepNumber: Int) {
    STEP_1_NAME_GENDER(1),
    STEP_2_AGE(2),
    STEP_3_HEIGHT_WEIGHT(3),
    STEP_4_ACTIVITY(4),
    STEP_5_GOAL_PACE(5)
}

enum class UnitSystem {
    METRIC,   // cm, kg
    IMPERIAL  // ft/in, lb
}

data class OnboardingUiState(
    val currentRoute: OnboardingScreenRoute = OnboardingScreenRoute.WELCOME,
    val currentWizardStep: WizardStep = WizardStep.STEP_1_NAME_GENDER,
    val welcomeSlideIndex: Int = 0,
    val isDisclaimerChecked: Boolean = false,

    // Step 1: Name & Gender
    val name: String = "",
    val gender: Gender = Gender.MALE,

    // Step 2: Age
    val age: Int = 25,
    val ageInputText: String = "25",
    val ageError: String? = null,

    // Step 3: Height & Weight
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val heightCm: Double = 175.0,
    val weightKg: Double = 75.0,
    val heightInputText: String = "175",
    val weightInputText: String = "75.0",
    val heightFeetInputText: String = "5",
    val heightInchesInputText: String = "9",
    val heightError: String? = null,
    val weightError: String? = null,

    // Step 4: Activity Level
    val activityLevel: ActivityLevel = ActivityLevel.MODERATELY_ACTIVE,

    // Step 5: Goal & Pace
    val goalType: GoalType = GoalType.LOSE_WEIGHT,
    val goalWeightKg: Double = 70.0,
    val goalWeightInputText: String = "70.0",
    val paceKgPerWeek: Double = 0.5,
    val goalWeightError: String? = null,

    // Calculated Targets & Plan
    val tdee: Double = 2325.0,
    val calorieGoalResult: CalorieGoalResult? = null,
    val targetCalories: Int = 1850,
    val macroPreset: MacroPreset = MacroPreset.BALANCED,
    val macroSplit: MacroSplit = MacroSplit(proteinGrams = 125, carbsGrams = 250, fatGrams = 56),
    val waterGoalMl: Int = 2500,
    val stepGoal: Int = 10000,
    val bmiResult: BmiResult? = null,

    // Plan Fine-tuning Slider
    val sliderCalorieValue: Float = 1850f,
    val isComplete: Boolean = false
)
