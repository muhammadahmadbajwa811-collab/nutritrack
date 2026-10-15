package com.nutritrack.app.data.repository

import com.nutritrack.app.core.database.dao.WaterDao
import com.nutritrack.app.core.database.entity.WaterLogEntity
import com.nutritrack.app.data.mapper.toDomainModel
import com.nutritrack.app.domain.model.WaterEntry
import com.nutritrack.app.domain.repository.WaterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaterRepositoryImpl @Inject constructor(
    private val waterDao: WaterDao
) : WaterRepository {

    override fun getLogsForDate(date: String): Flow<List<WaterEntry>> {
        return waterDao.getLogsForDate(date).map { list -> list.map { it.toDomainModel() } }
    }

    override fun getTotalWaterForDate(date: String): Flow<Int> {
        return waterDao.getTotalWaterForDate(date).map { it ?: 0 }
    }

    override suspend fun getTotalWaterForDateSync(date: String): Int {
        return waterDao.getTotalWaterForDateSync(date) ?: 0
    }

    override fun getLogsBetweenDates(startDate: String, endDate: String): Flow<List<WaterEntry>> {
        return waterDao.getLogsBetweenDates(startDate, endDate).map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun logWater(date: String, amountMl: Int) {
        val entity = WaterLogEntity(
            id = UUID.randomUUID().toString(),
            date = date,
            timestamp = System.currentTimeMillis(),
            amountMl = amountMl
        )
        waterDao.insertLog(entity)
    }

    override suspend fun deleteLog(id: String) {
        waterDao.deleteLogById(id)
    }

    override suspend fun clearDate(date: String) {
        waterDao.clearDate(date)
    }
}
