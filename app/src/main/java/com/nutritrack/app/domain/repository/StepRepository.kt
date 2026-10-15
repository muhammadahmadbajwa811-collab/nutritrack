package com.nutritrack.app.domain.repository

import com.nutritrack.app.domain.model.StepDaySummary
import com.nutritrack.app.domain.model.StepSource
import kotlinx.coroutines.flow.Flow

interface StepRepository {
    fun getStepDay(date: String): Flow<StepDaySummary?>
    suspend fun getStepDaySync(date: String): StepDaySummary?
    fun getStepsBetweenDates(startDate: String, endDate: String): Flow<List<StepDaySummary>>
    suspend fun updateSensorSteps(date: String, sensorSteps: Int)
    suspend fun updateHealthConnectSteps(date: String, healthConnectSteps: Int)
    suspend fun setManualSteps(date: String, manualSteps: Int?)
    suspend fun setStepSource(date: String, source: StepSource)
    suspend fun recordSensorSnapshot(counterValue: Long)
}
