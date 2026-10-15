package com.nutritrack.app.domain.repository

import com.nutritrack.app.domain.model.WeightEntry
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    fun getWeightForDate(date: String): Flow<WeightEntry?>
    suspend fun getWeightForDateSync(date: String): WeightEntry?
    fun getLatestWeight(): Flow<WeightEntry?>
    suspend fun getLatestWeightSync(): WeightEntry?
    fun getAllWeights(): Flow<List<WeightEntry>>
    suspend fun getAllWeightsSync(): List<WeightEntry>
    fun getWeightsBetweenDates(startDate: String, endDate: String): Flow<List<WeightEntry>>
    suspend fun logWeight(date: String, weightKg: Double, note: String? = null)
    suspend fun deleteWeight(date: String)
}
