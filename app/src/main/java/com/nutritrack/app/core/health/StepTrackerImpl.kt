package com.nutritrack.app.core.health

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepTrackerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StepTracker {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    override val isAvailable: Boolean
        get() = context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER) && stepSensor != null

    private val _steps = MutableStateFlow(0)
    override val stepCount: Flow<Int> = _steps.asStateFlow()

    override suspend fun startTracking() {
        // Baseline implementation for Phase 0 - full sensor integration implemented in Phase 5
    }

    override suspend fun stopTracking() {
        // Baseline implementation for Phase 0
    }
}
