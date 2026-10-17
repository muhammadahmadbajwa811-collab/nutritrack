package com.nutritrack.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nutritrack.app.designsystem.components.NtBottomBar
import com.nutritrack.app.domain.model.MealType
import com.nutritrack.app.feature.activity.ActivityScreen
import com.nutritrack.app.feature.diary.DiaryScreen
import com.nutritrack.app.feature.food.AddFoodBottomSheet
import com.nutritrack.app.feature.food.AddFoodViewModel
import com.nutritrack.app.feature.food.FoodDetailBottomSheet
import com.nutritrack.app.feature.food.FoodDetailViewModel
import com.nutritrack.app.feature.gallery.ComponentGalleryScreen
import com.nutritrack.app.feature.home.HomeScreen
import com.nutritrack.app.feature.onboarding.OnboardingScreen
import com.nutritrack.app.feature.progress.ProgressScreen
import com.nutritrack.app.feature.settings.SettingsScreen
import java.time.LocalDate

object Destinations {
    const val HOME = "home"
    const val DIARY = "diary"
    const val ACTIVITY = "activity"
    const val PROGRESS = "progress"
    const val SETTINGS = "settings"
    const val ONBOARDING = "onboarding"
    const val GALLERY = "gallery"
}

@Composable
fun NutriTrackNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Destinations.HOME
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route ?: Destinations.HOME

    val showBottomBar = currentRoute in listOf(
        Destinations.HOME,
        Destinations.DIARY,
        Destinations.ACTIVITY,
        Destinations.PROGRESS
    )

    // Global Add Food & Food Detail state triggered from centre FAB
    var showGlobalAddFoodSheet by remember { mutableStateOf(false) }
    var showGlobalFoodDetailSheet by remember { mutableStateOf(false) }
    val globalAddFoodViewModel: AddFoodViewModel = hiltViewModel()
    val globalFoodDetailViewModel: FoodDetailViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NtBottomBar(
                    currentRoute = currentRoute,
                    onNavigateToTab = { route ->
                        navController.navigate(route) {
                            popUpTo(Destinations.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAddClick = {
                        globalAddFoodViewModel.setInitialMeal(AddFoodViewModel_defaultMeal())
                        showGlobalAddFoodSheet = true
                    }
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destinations.HOME) {
                HomeScreen(
                    onNavigateToSettings = {
                        navController.navigate(Destinations.SETTINGS)
                    },
                    onNavigateToDiaryDate = { _ ->
                        navController.navigate(Destinations.DIARY)
                    }
                )
            }
            composable(Destinations.DIARY) {
                DiaryScreen()
            }
            composable(Destinations.ACTIVITY) {
                ActivityScreen()
            }
            composable(Destinations.PROGRESS) {
                ProgressScreen()
            }
            composable(Destinations.SETTINGS) {
                SettingsScreen(
                    onNavigateToGallery = {
                        navController.navigate(Destinations.GALLERY)
                    }
                )
            }
            composable(Destinations.ONBOARDING) {
                OnboardingScreen(
                    onOnboardingFinished = {
                        navController.navigate(Destinations.HOME) {
                            popUpTo(Destinations.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
            composable(Destinations.GALLERY) {
                ComponentGalleryScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }

    // Global Add Food Sheet from Bottom Bar FAB
    if (showGlobalAddFoodSheet) {
        AddFoodBottomSheet(
            viewModel = globalAddFoodViewModel,
            targetDate = LocalDate.now(),
            onDismiss = { showGlobalAddFoodSheet = false },
            onSelectFood = { foodId, meal, date ->
                globalFoodDetailViewModel.loadFood(foodId, meal, date)
                showGlobalFoodDetailSheet = true
            }
        )
    }

    // Global Food Detail Sheet from Bottom Bar FAB
    if (showGlobalFoodDetailSheet) {
        FoodDetailBottomSheet(
            viewModel = globalFoodDetailViewModel,
            onDismiss = { showGlobalFoodDetailSheet = false },
            onEntrySaved = {
                showGlobalFoodDetailSheet = false
                showGlobalAddFoodSheet = false
            }
        )
    }
}

private fun AddFoodViewModel_defaultMeal(): MealType {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 11 -> MealType.BREAKFAST
        hour < 16 -> MealType.LUNCH
        hour < 21 -> MealType.DINNER
        else -> MealType.SNACKS
    }
}
