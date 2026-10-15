package com.nutritrack.app.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NutriTrackPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        
        // Units
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit") // "KG", "LB"
        val HEIGHT_UNIT = stringPreferencesKey("height_unit") // "CM", "FT_IN"
        val VOLUME_UNIT = stringPreferencesKey("volume_unit") // "ML", "FL_OZ"
        val ENERGY_UNIT = stringPreferencesKey("energy_unit") // "KCAL", "KJ"
        val LANGUAGE = stringPreferencesKey("language") // "EN", "UR"

        // Goals & Tracking
        val DAILY_CALORIE_GOAL = intPreferencesKey("daily_calorie_goal")
        val DAILY_WATER_GOAL_ML = intPreferencesKey("daily_water_goal_ml")
        val DAILY_STEP_GOAL = intPreferencesKey("daily_step_goal")
        val STEP_SOURCE = stringPreferencesKey("step_source") // "AUTO", "HEALTH_CONNECT", "SENSOR", "MANUAL"
        val FAVOURITE_GLASS_ML = intPreferencesKey("favourite_glass_ml")

        // Reminders
        val REMINDER_BREAKFAST_ENABLED = booleanPreferencesKey("reminder_breakfast_enabled")
        val REMINDER_BREAKFAST_TIME = stringPreferencesKey("reminder_breakfast_time")
        val REMINDER_LUNCH_ENABLED = booleanPreferencesKey("reminder_lunch_enabled")
        val REMINDER_LUNCH_TIME = stringPreferencesKey("reminder_lunch_time")
        val REMINDER_DINNER_ENABLED = booleanPreferencesKey("reminder_dinner_enabled")
        val REMINDER_DINNER_TIME = stringPreferencesKey("reminder_dinner_time")
        val REMINDER_HYDRATION_ENABLED = booleanPreferencesKey("reminder_hydration_enabled")
        val REMINDER_HYDRATION_INTERVAL = intPreferencesKey("reminder_hydration_interval")
        val QUIET_HOURS_START = stringPreferencesKey("quiet_hours_start")
        val QUIET_HOURS_END = stringPreferencesKey("quiet_hours_end")
    }

    private fun <T> getPreferenceFlow(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences ->
                preferences[key] ?: defaultValue
            }
    }

    // Onboarding
    val isOnboardingCompleted: Flow<Boolean> = getPreferenceFlow(Keys.ONBOARDING_COMPLETED, false)
    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    // Theme & Motion
    val themeMode: Flow<String> = getPreferenceFlow(Keys.THEME_MODE, "SYSTEM")
    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    val isDynamicColorEnabled: Flow<Boolean> = getPreferenceFlow(Keys.DYNAMIC_COLOR, false)
    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    val isReduceMotionEnabled: Flow<Boolean> = getPreferenceFlow(Keys.REDUCE_MOTION, false)
    suspend fun setReduceMotionEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.REDUCE_MOTION] = enabled }
    }

    val isHapticsEnabled: Flow<Boolean> = getPreferenceFlow(Keys.HAPTICS_ENABLED, true)
    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    // Units
    val weightUnit: Flow<String> = getPreferenceFlow(Keys.WEIGHT_UNIT, "KG")
    suspend fun setWeightUnit(unit: String) {
        dataStore.edit { it[Keys.WEIGHT_UNIT] = unit }
    }

    val heightUnit: Flow<String> = getPreferenceFlow(Keys.HEIGHT_UNIT, "CM")
    suspend fun setHeightUnit(unit: String) {
        dataStore.edit { it[Keys.HEIGHT_UNIT] = unit }
    }

    val volumeUnit: Flow<String> = getPreferenceFlow(Keys.VOLUME_UNIT, "ML")
    suspend fun setVolumeUnit(unit: String) {
        dataStore.edit { it[Keys.VOLUME_UNIT] = unit }
    }

    val energyUnit: Flow<String> = getPreferenceFlow(Keys.ENERGY_UNIT, "KCAL")
    suspend fun setEnergyUnit(unit: String) {
        dataStore.edit { it[Keys.ENERGY_UNIT] = unit }
    }

    val language: Flow<String> = getPreferenceFlow(Keys.LANGUAGE, "EN")
    suspend fun setLanguage(lang: String) {
        dataStore.edit { it[Keys.LANGUAGE] = lang }
    }

    // Targets
    val dailyCalorieGoal: Flow<Int> = getPreferenceFlow(Keys.DAILY_CALORIE_GOAL, 2000)
    suspend fun setDailyCalorieGoal(goal: Int) {
        dataStore.edit { it[Keys.DAILY_CALORIE_GOAL] = goal }
    }

    val dailyWaterGoalMl: Flow<Int> = getPreferenceFlow(Keys.DAILY_WATER_GOAL_ML, 2500)
    suspend fun setDailyWaterGoalMl(goal: Int) {
        dataStore.edit { it[Keys.DAILY_WATER_GOAL_ML] = goal }
    }

    val dailyStepGoal: Flow<Int> = getPreferenceFlow(Keys.DAILY_STEP_GOAL, 10000)
    suspend fun setDailyStepGoal(goal: Int) {
        dataStore.edit { it[Keys.DAILY_STEP_GOAL] = goal }
    }

    val stepSource: Flow<String> = getPreferenceFlow(Keys.STEP_SOURCE, "AUTO")
    suspend fun setStepSource(source: String) {
        dataStore.edit { it[Keys.STEP_SOURCE] = source }
    }

    val favouriteGlassMl: Flow<Int> = getPreferenceFlow(Keys.FAVOURITE_GLASS_ML, 250)
    suspend fun setFavouriteGlassMl(ml: Int) {
        dataStore.edit { it[Keys.FAVOURITE_GLASS_ML] = ml }
    }

    // Reminders
    val isBreakfastReminderEnabled: Flow<Boolean> = getPreferenceFlow(Keys.REMINDER_BREAKFAST_ENABLED, false)
    suspend fun setBreakfastReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.REMINDER_BREAKFAST_ENABLED] = enabled }
    }

    val breakfastReminderTime: Flow<String> = getPreferenceFlow(Keys.REMINDER_BREAKFAST_TIME, "08:30")
    suspend fun setBreakfastReminderTime(time: String) {
        dataStore.edit { it[Keys.REMINDER_BREAKFAST_TIME] = time }
    }

    val isLunchReminderEnabled: Flow<Boolean> = getPreferenceFlow(Keys.REMINDER_LUNCH_ENABLED, false)
    suspend fun setLunchReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.REMINDER_LUNCH_ENABLED] = enabled }
    }

    val lunchReminderTime: Flow<String> = getPreferenceFlow(Keys.REMINDER_LUNCH_TIME, "13:30")
    suspend fun setLunchReminderTime(time: String) {
        dataStore.edit { it[Keys.REMINDER_LUNCH_TIME] = time }
    }

    val isDinnerReminderEnabled: Flow<Boolean> = getPreferenceFlow(Keys.REMINDER_DINNER_ENABLED, false)
    suspend fun setDinnerReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.REMINDER_DINNER_ENABLED] = enabled }
    }

    val dinnerReminderTime: Flow<String> = getPreferenceFlow(Keys.REMINDER_DINNER_TIME, "20:00")
    suspend fun setDinnerReminderTime(time: String) {
        dataStore.edit { it[Keys.REMINDER_DINNER_TIME] = time }
    }

    val isHydrationReminderEnabled: Flow<Boolean> = getPreferenceFlow(Keys.REMINDER_HYDRATION_ENABLED, false)
    suspend fun setHydrationReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.REMINDER_HYDRATION_ENABLED] = enabled }
    }

    val hydrationReminderIntervalMinutes: Flow<Int> = getPreferenceFlow(Keys.REMINDER_HYDRATION_INTERVAL, 120)
    suspend fun setHydrationReminderIntervalMinutes(minutes: Int) {
        dataStore.edit { it[Keys.REMINDER_HYDRATION_INTERVAL] = minutes }
    }

    val quietHoursStart: Flow<String> = getPreferenceFlow(Keys.QUIET_HOURS_START, "22:00")
    suspend fun setQuietHoursStart(time: String) {
        dataStore.edit { it[Keys.QUIET_HOURS_START] = time }
    }

    val quietHoursEnd: Flow<String> = getPreferenceFlow(Keys.QUIET_HOURS_END, "08:00")
    suspend fun setQuietHoursEnd(time: String) {
        dataStore.edit { it[Keys.QUIET_HOURS_END] = time }
    }
}
