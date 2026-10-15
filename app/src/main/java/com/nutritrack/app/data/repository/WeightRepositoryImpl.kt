package com.nutritrack.app.data.repository

import com.nutritrack.app.core.database.dao.WeightDao
import com.nutritrack.app.core.database.entity.WeightLogEntity
import com.nutritrack.app.data.mapper.toDomainModel
import com.nutritrack.app.domain.model.WeightEntry
import com.nutritrack.app.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepositoryImpl @Inject constructor(
    private val weightDao: WeightDao
) : WeightRepository {

    override fun getWeightForDate(date: String): Flow<WeightEntry?> {
        return weightDao.getWeightForDate(date).map { it?.toDomainModel() }
    }

    override suspend fun getWeightForDateSync(date: String): WeightEntry? {
        return weightDao.getWeightForDateSync(date)?.toDomainModel()
    }

    override fun getLatestWeight(): Flow<WeightEntry?> {
        return weightDao.getLatestWeight().map { it?.toDomainModel() }
    }

    override suspend fun getLatestWeightSync(): WeightEntry? {
        return weightDao.getLatestWeightSync()?.toDomainModel()
    }

    override fun getAllWeights(): Flow<List<WeightEntry>> {
        return weightDao.getAllWeights().map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun getAllWeightsSync(): List<WeightEntry> {
        return weightDao.getAllWeightsSync().map { it.toDomainModel() }
    }

    override fun getWeightsBetweenDates(startDate: String, endDate: String): Flow<List<WeightEntry>> {
        return weightDao.getWeightsBetweenDates(startDate, endDate).map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun logWeight(date: String, weightKg: Double, note: String?) {
        val existing = weightDao.getWeightForDateSync(date)
        val entity = WeightLogEntity(
            id = existing?.id ?: UUID.randomUUID().toString(),
            date = date,
            weightKg = weightKg,
            note = note,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )
        weightDao.insertOrUpdateWeight(entity)
    }

    override suspend fun deleteWeight(date: String) {
        weightDao.deleteWeightForDate(date)
    }
}
