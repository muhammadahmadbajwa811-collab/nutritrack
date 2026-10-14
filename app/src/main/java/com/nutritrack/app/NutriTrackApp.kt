package com.nutritrack.app

import android.app.Application
import com.nutritrack.app.core.notifications.NotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NutriTrackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createChannels(this)
    }
}
