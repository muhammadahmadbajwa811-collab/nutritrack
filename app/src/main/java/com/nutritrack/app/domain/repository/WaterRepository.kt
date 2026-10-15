package com.nutritrack.app.domain.repository

import com.nutritrack.app.domain.model.WaterEntry
import kotlinx.coroutines.flow.Flow

interface WaterRepository {
    fun getLogsForDate(date: String): Flow<List<WaterEntry>>
    fun getTotalWaterForDate(date: String): Flow<Int>
    suspend fun getTotalWaterForDateSync(date: String): Int
    fun getLogsBetweenDates(startDate: String, endDate: String): Flow<List<WaterEntry>>
    suspend fun logWater(date: String, amountMl: Int)
    suspend fun deleteLog(id: String)
    suspend fun clearDate(date: String)
}
