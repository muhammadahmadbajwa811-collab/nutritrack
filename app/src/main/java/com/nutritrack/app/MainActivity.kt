package com.nutritrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutritrack.app.designsystem.theme.NutriTrackTheme
import com.nutritrack.app.navigation.Destinations
import com.nutritrack.app.navigation.NutriTrackNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            mainViewModel.isLoading.value
        }

        setContent {
            val isOnboardingCompleted by mainViewModel.isOnboardingCompleted.collectAsStateWithLifecycle(initialValue = null)

            NutriTrackTheme {
                if (isOnboardingCompleted != null) {
                    val startDestination = if (isOnboardingCompleted == true) {
                        Destinations.HOME
                    } else {
                        Destinations.ONBOARDING
                    }
                    NutriTrackNavHost(startDestination = startDestination)
                }
            }
        }
    }
}

