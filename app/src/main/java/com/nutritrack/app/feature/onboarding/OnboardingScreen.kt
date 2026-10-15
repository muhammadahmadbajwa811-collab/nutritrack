package com.nutritrack.app.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit = {},
    viewModel: OnboardingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onOnboardingFinished()
        }
    }

    AnimatedContent(
        targetState = uiState.currentRoute,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "OnboardingRouteTransition",
        modifier = modifier.fillMaxSize()
    ) { route ->
        when (route) {
            OnboardingScreenRoute.WELCOME -> {
                WelcomeCarouselScreen(
                    currentSlideIndex = uiState.welcomeSlideIndex,
                    onSlideChanged = viewModel::onWelcomeSlideChanged,
                    onNextClicked = viewModel::onWelcomeNextClicked,
                    onSkipClicked = viewModel::onWelcomeSkipClicked
                )
            }
            OnboardingScreenRoute.DISCLAIMER -> {
                HealthDisclaimerScreen(
                    isChecked = uiState.isDisclaimerChecked,
                    onCheckedChange = viewModel::onDisclaimerCheckedChanged,
                    onContinueClicked = viewModel::onDisclaimerContinueClicked
                )
            }
            OnboardingScreenRoute.WIZARD -> {
                ProfileSetupWizardScreen(
                    uiState = uiState,
                    onBackClicked = viewModel::onWizardBackClicked,
                    onNextClicked = viewModel::onWizardNextClicked,
                    onNameChanged = viewModel::onNameChanged,
                    onGenderChanged = viewModel::onGenderChanged,
                    onAgeChanged = viewModel::onAgeChanged,
                    onIncrementAge = viewModel::incrementAge,
                    onDecrementAge = viewModel::decrementAge,
                    onUnitSystemToggled = viewModel::onUnitSystemToggled,
                    onHeightCmChanged = viewModel::onHeightCmChanged,
                    onHeightFeetChanged = viewModel::onHeightFeetChanged,
                    onHeightInchesChanged = viewModel::onHeightInchesChanged,
                    onWeightChanged = viewModel::onWeightChanged,
                    onActivityLevelChanged = viewModel::onActivityLevelChanged,
                    onGoalTypeChanged = viewModel::onGoalTypeChanged,
                    onGoalWeightChanged = viewModel::onGoalWeightChanged,
                    onPaceChanged = viewModel::onPaceChanged
                )
            }
            OnboardingScreenRoute.PLAN_REVEAL -> {
                PlanRevealScreen(
                    uiState = uiState,
                    onBackClicked = viewModel::onWizardBackClicked,
                    onSliderCalorieChanged = viewModel::onSliderCalorieChanged,
                    onStartTrackingClicked = viewModel::completeOnboarding
                )
            }
        }
    }
}
