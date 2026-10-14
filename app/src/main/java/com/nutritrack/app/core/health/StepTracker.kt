package com.nutritrack.app.core.health

import kotlinx.coroutines.flow.Flow

interface StepTracker {
    val stepCount: Flow<Int>
    val isAvailable: Boolean
    suspend fun startTracking()
    suspend fun stopTracking()
}
