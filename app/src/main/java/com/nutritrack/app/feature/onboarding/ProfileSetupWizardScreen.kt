package com.nutritrack.app.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Weekend
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutritrack.app.designsystem.components.NtButton
import com.nutritrack.app.designsystem.components.NtChip
import com.nutritrack.app.designsystem.components.NtTextField
import com.nutritrack.app.designsystem.theme.InfoLight
import com.nutritrack.app.designsystem.theme.SuccessLight
import com.nutritrack.app.designsystem.theme.WarningLight
import com.nutritrack.app.designsystem.theme.nutriTrackColors
import com.nutritrack.app.domain.calc.BmiCategory
import com.nutritrack.app.domain.model.ActivityLevel
import com.nutritrack.app.domain.model.Gender
import com.nutritrack.app.domain.model.GoalType
import java.util.Locale

@Composable
fun ProfileSetupWizardScreen(
    uiState: OnboardingUiState,
    onBackClicked: () -> Unit,
    onNextClicked: () -> Unit,
    onNameChanged: (String) -> Unit,
    onGenderChanged: (Gender) -> Unit,
    onAgeChanged: (String) -> Unit,
    onIncrementAge: () -> Unit,
    onDecrementAge: () -> Unit,
    onUnitSystemToggled: (UnitSystem) -> Unit,
    onHeightCmChanged: (String) -> Unit,
    onHeightFeetChanged: (String) -> Unit,
    onHeightInchesChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onActivityLevelChanged: (ActivityLevel) -> Unit,
    onGoalTypeChanged: (GoalType) -> Unit,
    onGoalWeightChanged: (String) -> Unit,
    onPaceChanged: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // --- Top Bar: Back Button & 5-Segment Progress Indicator ---
        WizardTopBar(
            currentStep = uiState.currentWizardStep,
            onBackClicked = onBackClicked
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Step Content Area (Scrollable) ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = uiState.currentWizardStep,
                transitionSpec = {
                    if (targetState.stepNumber > initialState.stepNumber) {
                        (slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn()).togetherWith(
                            slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn()).togetherWith(
                            slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut()
                        )
                    }
                },
                label = "WizardStepTransition"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    when (step) {
                        WizardStep.STEP_1_NAME_GENDER -> {
                            Step1NameAndGender(
                                name = uiState.name,
                                gender = uiState.gender,
                                onNameChanged = onNameChanged,
                                onGenderChanged = onGenderChanged
                            )
                        }
                        WizardStep.STEP_2_AGE -> {
                            Step2Age(
                                age = uiState.age,
                                ageInputText = uiState.ageInputText,
                                ageError = uiState.ageError,
                                onAgeChanged = onAgeChanged,
                                onIncrementAge = onIncrementAge,
                                onDecrementAge = onDecrementAge
                            )
                        }
                        WizardStep.STEP_3_HEIGHT_WEIGHT -> {
                            Step3HeightWeight(
                                unitSystem = uiState.unitSystem,
                                heightInputText = uiState.heightInputText,
                                heightFeetInputText = uiState.heightFeetInputText,
                                heightInchesInputText = uiState.heightInchesInputText,
                                weightInputText = uiState.weightInputText,
                                heightError = uiState.heightError,
                                weightError = uiState.weightError,
                                bmiResult = uiState.bmiResult,
                                onUnitSystemToggled = onUnitSystemToggled,
                                onHeightCmChanged = onHeightCmChanged,
                                onHeightFeetChanged = onHeightFeetChanged,
                                onHeightInchesChanged = onHeightInchesChanged,
                                onWeightChanged = onWeightChanged
                            )
                        }
                        WizardStep.STEP_4_ACTIVITY -> {
                            Step4ActivityLevel(
                                selectedLevel = uiState.activityLevel,
                                onActivityLevelChanged = onActivityLevelChanged
                            )
                        }
                        WizardStep.STEP_5_GOAL_PACE -> {
                            Step5GoalAndPace(
                                goalType = uiState.goalType,
                                goalWeightInputText = uiState.goalWeightInputText,
                                goalWeightError = uiState.goalWeightError,
                                unitSystem = uiState.unitSystem,
                                paceKgPerWeek = uiState.paceKgPerWeek,
                                onGoalTypeChanged = onGoalTypeChanged,
                                onGoalWeightChanged = onGoalWeightChanged,
                                onPaceChanged = onPaceChanged
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // --- Bottom-Pinned Navigation Button ---
        val isLastStep = uiState.currentWizardStep == WizardStep.STEP_5_GOAL_PACE
        NtButton(
            text = if (isLastStep) "Generate My Plan" else "Continue",
            onClick = onNextClicked,
            trailingIcon = if (isLastStep) Icons.Rounded.Check else Icons.Rounded.ArrowForward,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ---------------------------------------------------------------------------
// Top Bar with 5-segment animated indicator
// ---------------------------------------------------------------------------

@Composable
private fun WizardTopBar(
    currentStep: WizardStep,
    onBackClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClicked,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Go back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 5 Progress segments
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..5) {
                val isCompletedOrCurrent = i <= currentStep.stepNumber
                val segmentColor by animateColorAsState(
                    targetValue = if (isCompletedOrCurrent) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    },
                    animationSpec = tween(250),
                    label = "segmentColor_$i"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(segmentColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "${currentStep.stepNumber}/5",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------------------------
// Step 1: Name & Gender
// ---------------------------------------------------------------------------

@Composable
private fun Step1NameAndGender(
    name: String,
    gender: Gender,
    onNameChanged: (String) -> Unit,
    onGenderChanged: (Gender) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Let's personalize your plan",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "NutriTrack uses your biological sex to accurately apply the Mifflin-St Jeor metabolic equations.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Name input
        NtTextField(
            value = name,
            onValueChange = onNameChanged,
            label = "Your Name (Optional)",
            placeholder = "e.g. Sarah or Ali",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Biological Sex",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelectableGenderCard(
                gender = Gender.MALE,
                isSelected = gender == Gender.MALE,
                icon = Icons.Rounded.Male,
                label = "Male",
                formulaNote = "+5 kcal/day (MSJ)",
                onSelect = { onGenderChanged(Gender.MALE) },
                modifier = Modifier.weight(1f)
            )

            SelectableGenderCard(
                gender = Gender.FEMALE,
                isSelected = gender == Gender.FEMALE,
                icon = Icons.Rounded.Female,
                label = "Female",
                formulaNote = "-161 kcal/day (MSJ)",
                onSelect = { onGenderChanged(Gender.FEMALE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SelectableGenderCard(
    gender: Gender,
    isSelected: Boolean,
    icon: ImageVector,
    label: String,
    formulaNote: String,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                contentDescription = "$label biological sex"
            }
            .clickable(onClick = onSelect)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formulaNote,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Step 2: Age
// ---------------------------------------------------------------------------

@Composable
private fun Step2Age(
    age: Int,
    ageInputText: String,
    ageError: String?,
    onAgeChanged: (String) -> Unit,
    onIncrementAge: () -> Unit,
    onDecrementAge: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "How old are you?",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Metabolic rate changes as you age. This helps NutriTrack estimate your exact resting energy expenditure.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Age Stepper Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Age",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Decrement Button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .size(54.dp)
                            .clickable(enabled = age > 14, onClick = onDecrementAge)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Remove,
                                contentDescription = "Decrease age",
                                tint = if (age > 14) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$age",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "years old",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    // Increment Button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .size(54.dp)
                            .clickable(enabled = age < 100, onClick = onIncrementAge)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Increase age",
                                tint = if (age < 100) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Direct Text input for fast entry
        NtTextField(
            value = ageInputText,
            onValueChange = onAgeChanged,
            label = "Or type your age",
            placeholder = "25",
            unitSuffix = "yrs",
            isError = ageError != null,
            errorMessage = ageError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        )
    }
}

// ---------------------------------------------------------------------------
// Step 3: Height & Weight
// ---------------------------------------------------------------------------

@Composable
private fun Step3HeightWeight(
    unitSystem: UnitSystem,
    heightInputText: String,
    heightFeetInputText: String,
    heightInchesInputText: String,
    weightInputText: String,
    heightError: String?,
    weightError: String?,
    bmiResult: com.nutritrack.app.domain.calc.BmiResult?,
    onUnitSystemToggled: (UnitSystem) -> Unit,
    onHeightCmChanged: (String) -> Unit,
    onHeightFeetChanged: (String) -> Unit,
    onHeightInchesChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Your measurements",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "NutriTrack computes your Body Mass Index (BMI) and baseline caloric burn from these values.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Unit Toggle Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            NtChip(
                label = "Metric (cm, kg)",
                isSelected = unitSystem == UnitSystem.METRIC,
                onClick = { onUnitSystemToggled(UnitSystem.METRIC) }
            )

            Spacer(modifier = Modifier.width(12.dp))

            NtChip(
                label = "Imperial (ft/in, lb)",
                isSelected = unitSystem == UnitSystem.IMPERIAL,
                onClick = { onUnitSystemToggled(UnitSystem.IMPERIAL) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Height Field(s)
        if (unitSystem == UnitSystem.METRIC) {
            NtTextField(
                value = heightInputText,
                onValueChange = onHeightCmChanged,
                label = "Height",
                placeholder = "175",
                unitSuffix = "cm",
                isError = heightError != null,
                errorMessage = heightError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NtTextField(
                    value = heightFeetInputText,
                    onValueChange = onHeightFeetChanged,
                    label = "Feet",
                    placeholder = "5",
                    unitSuffix = "ft",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f)
                )

                NtTextField(
                    value = heightInchesInputText,
                    onValueChange = onHeightInchesChanged,
                    label = "Inches",
                    placeholder = "9",
                    unitSuffix = "in",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weight Field
        NtTextField(
            value = weightInputText,
            onValueChange = onWeightChanged,
            label = "Current Weight",
            placeholder = if (unitSystem == UnitSystem.METRIC) "75.0" else "165.0",
            unitSuffix = if (unitSystem == UnitSystem.METRIC) "kg" else "lb",
            isError = weightError != null,
            errorMessage = weightError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Live BMI Card
        if (bmiResult != null && bmiResult.bmi > 0.0) {
            BmiPreviewCard(bmiResult = bmiResult)
        }
    }
}

@Composable
private fun BmiPreviewCard(bmiResult: com.nutritrack.app.domain.calc.BmiResult) {
    val categoryColor = when (bmiResult.category) {
        BmiCategory.HEALTHY -> SuccessLight
        BmiCategory.UNDERWEIGHT -> InfoLight
        BmiCategory.OVERWEIGHT -> WarningLight
        BmiCategory.OBESE -> MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Body Mass Index",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format(Locale.US, "%.1f BMI", bmiResult.bmi),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(categoryColor.copy(alpha = 0.15f))
                    .border(1.dp, categoryColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = bmiResult.category.label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Step 4: Activity Level
// ---------------------------------------------------------------------------

@Composable
private fun Step4ActivityLevel(
    selectedLevel: ActivityLevel,
    onActivityLevelChanged: (ActivityLevel) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "How active are you?",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Factor in your daily movement and exercise to calculate your Total Daily Energy Expenditure (TDEE).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ActivityCard(
                level = ActivityLevel.SEDENTARY,
                isSelected = selectedLevel == ActivityLevel.SEDENTARY,
                icon = Icons.Rounded.Weekend,
                title = "Sedentary",
                description = "Desk job, minimal movement during the day",
                multiplierBadge = "1.2x TDEE • 8,000 steps",
                onSelect = { onActivityLevelChanged(ActivityLevel.SEDENTARY) }
            )

            ActivityCard(
                level = ActivityLevel.LIGHTLY_ACTIVE,
                isSelected = selectedLevel == ActivityLevel.LIGHTLY_ACTIVE,
                icon = Icons.Rounded.DirectionsWalk,
                title = "Lightly active",
                description = "Light exercise or sports 1–3 days/week",
                multiplierBadge = "1.375x TDEE • 10,000 steps",
                onSelect = { onActivityLevelChanged(ActivityLevel.LIGHTLY_ACTIVE) }
            )

            ActivityCard(
                level = ActivityLevel.MODERATELY_ACTIVE,
                isSelected = selectedLevel == ActivityLevel.MODERATELY_ACTIVE,
                icon = Icons.Rounded.FitnessCenter,
                title = "Moderately active",
                description = "Moderate exercise or sports 3–5 days/week",
                multiplierBadge = "1.55x TDEE • 10,000 steps",
                onSelect = { onActivityLevelChanged(ActivityLevel.MODERATELY_ACTIVE) }
            )

            ActivityCard(
                level = ActivityLevel.VERY_ACTIVE,
                isSelected = selectedLevel == ActivityLevel.VERY_ACTIVE,
                icon = Icons.Rounded.DirectionsRun,
                title = "Very active",
                description = "Hard training 6–7 days/week or physical labour",
                multiplierBadge = "1.725x TDEE • 12,000 steps",
                onSelect = { onActivityLevelChanged(ActivityLevel.VERY_ACTIVE) }
            )
        }
    }
}

@Composable
private fun ActivityCard(
    level: ActivityLevel,
    isSelected: Boolean,
    icon: ImageVector,
    title: String,
    description: String,
    multiplierBadge: String,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                contentDescription = "$title, $multiplierBadge"
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = multiplierBadge,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Step 5: Goal & Pace
// ---------------------------------------------------------------------------

@Composable
private fun Step5GoalAndPace(
    goalType: GoalType,
    goalWeightInputText: String,
    goalWeightError: String?,
    unitSystem: UnitSystem,
    paceKgPerWeek: Double,
    onGoalTypeChanged: (GoalType) -> Unit,
    onGoalWeightChanged: (String) -> Unit,
    onPaceChanged: (Double) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "What is your main goal?",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We will balance your daily calorie intake and macronutrients to support your target.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3 Goal Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GoalCard(
                goalType = GoalType.LOSE_WEIGHT,
                isSelected = goalType == GoalType.LOSE_WEIGHT,
                icon = Icons.Rounded.TrendingDown,
                title = "Lose",
                subtitle = "Deficit",
                onSelect = { onGoalTypeChanged(GoalType.LOSE_WEIGHT) },
                modifier = Modifier.weight(1f)
            )

            GoalCard(
                goalType = GoalType.MAINTAIN_WEIGHT,
                isSelected = goalType == GoalType.MAINTAIN_WEIGHT,
                icon = Icons.Rounded.Balance,
                title = "Maintain",
                subtitle = "Balance",
                onSelect = { onGoalTypeChanged(GoalType.MAINTAIN_WEIGHT) },
                modifier = Modifier.weight(1f)
            )

            GoalCard(
                goalType = GoalType.GAIN_WEIGHT,
                isSelected = goalType == GoalType.GAIN_WEIGHT,
                icon = Icons.Rounded.TrendingUp,
                title = "Gain",
                subtitle = "Surplus",
                onSelect = { onGoalTypeChanged(GoalType.GAIN_WEIGHT) },
                modifier = Modifier.weight(1f)
            )
        }

        // Target weight & Pace options if Lose or Gain
        if (goalType != GoalType.MAINTAIN_WEIGHT) {
            Spacer(modifier = Modifier.height(24.dp))

            NtTextField(
                value = goalWeightInputText,
                onValueChange = onGoalWeightChanged,
                label = "Target Weight",
                placeholder = if (unitSystem == UnitSystem.METRIC) "70.0" else "155.0",
                unitSuffix = if (unitSystem == UnitSystem.METRIC) "kg" else "lb",
                isError = goalWeightError != null,
                errorMessage = goalWeightError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Target Weekly Pace",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val label025 = if (unitSystem == UnitSystem.METRIC) "0.25 kg/wk • Gentle" else "0.5 lb/wk • Gentle"
                    val label050 = if (unitSystem == UnitSystem.METRIC) "0.5 kg/wk • Recommended" else "1.0 lb/wk • Recommended"

                    PaceSelectChip(
                        label = label025,
                        isSelected = paceKgPerWeek == 0.25,
                        onClick = { onPaceChanged(0.25) },
                        modifier = Modifier.weight(1f)
                    )

                    PaceSelectChip(
                        label = label050,
                        isSelected = paceKgPerWeek == 0.5,
                        onClick = { onPaceChanged(0.5) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val label075 = if (unitSystem == UnitSystem.METRIC) "0.75 kg/wk • Steady" else "1.5 lb/wk • Steady"
                    val label100 = if (unitSystem == UnitSystem.METRIC) "1.0 kg/wk • Intensive" else "2.0 lb/wk • Intensive"

                    PaceSelectChip(
                        label = label075,
                        isSelected = paceKgPerWeek == 0.75,
                        onClick = { onPaceChanged(0.75) },
                        modifier = Modifier.weight(1f)
                    )

                    PaceSelectChip(
                        label = label100,
                        isSelected = paceKgPerWeek == 1.0,
                        onClick = { onPaceChanged(1.0) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalCard(
    goalType: GoalType,
    isSelected: Boolean,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .clickable(onClick = onSelect)
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                contentDescription = "$title goal, $subtitle"
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PaceSelectChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
