package com.nutritrack.app.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val CHANNEL_MEAL_REMINDERS = "meal_reminders"
    const val CHANNEL_WATER_REMINDERS = "water_reminders"
    const val CHANNEL_STEP_GOAL = "step_goal"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val mealChannel = NotificationChannel(
                CHANNEL_MEAL_REMINDERS,
                "Meal Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to log breakfast, lunch, dinner, and snacks"
            }

            val waterChannel = NotificationChannel(
                CHANNEL_WATER_REMINDERS,
                "Water Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to stay hydrated throughout the day"
            }

            val stepChannel = NotificationChannel(
                CHANNEL_STEP_GOAL,
                "Step & Activity Alerts",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications when you reach your daily step or activity milestones"
            }

            notificationManager.createNotificationChannels(
                listOf(mealChannel, waterChannel, stepChannel)
            )
        }
    }
}
