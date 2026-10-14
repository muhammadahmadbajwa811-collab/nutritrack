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

import com.nutritrack.app.feature.activity.ActivityScreen
import com.nutritrack.app.feature.diary.DiaryScreen
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
}

@Composable
fun NutriTrackNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Destinations.HOME
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
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
                SettingsScreen()
            }
            composable(Destinations.ONBOARDING) {
                OnboardingScreen()
            }
        }
    }
}
