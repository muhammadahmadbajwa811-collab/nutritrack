package com.nutritrack.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import androidx.navigation.compose.currentBackStackEntryAsState
import com.nutritrack.app.designsystem.components.NtBottomBar
import com.nutritrack.app.feature.activity.ActivityScreen
import com.nutritrack.app.feature.diary.DiaryScreen
import com.nutritrack.app.feature.gallery.ComponentGalleryScreen
import com.nutritrack.app.feature.home.HomeScreen
import com.nutritrack.app.feature.onboarding.OnboardingScreen
import com.nutritrack.app.feature.progress.ProgressScreen
import com.nutritrack.app.feature.settings.SettingsScreen

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
                        // In future phases: opens Add Food sheet
                        navController.navigate(Destinations.DIARY)
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
                HomeScreen()
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
                OnboardingScreen()
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
}

