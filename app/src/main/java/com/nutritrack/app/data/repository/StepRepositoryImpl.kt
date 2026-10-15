package com.nutritrack.app.data.repository

import com.nutritrack.app.core.database.dao.StepDao
import com.nutritrack.app.core.database.entity.StepDayEntity
import com.nutritrack.app.core.database.entity.StepSnapshotEntity
import com.nutritrack.app.data.mapper.toDomainModel
import com.nutritrack.app.domain.model.StepDaySummary
import com.nutritrack.app.domain.model.StepSource
import com.nutritrack.app.domain.repository.StepRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepRepositoryImpl @Inject constructor(
    private val stepDao: StepDao
) : StepRepository {

    override fun getStepDay(date: String): Flow<StepDaySummary?> {
        return stepDao.getStepDay(date).map { it?.toDomainModel() }
    }

    override suspend fun getStepDaySync(date: String): StepDaySummary? {
        return stepDao.getStepDaySync(date)?.toDomainModel()
    }

    override fun getStepsBetweenDates(startDate: String, endDate: String): Flow<List<StepDaySummary>> {
        return stepDao.getStepsBetweenDates(startDate, endDate).map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun updateSensorSteps(date: String, sensorSteps: Int) {
        val existing = stepDao.getStepDaySync(date)
        val updated = existing?.copy(sensorSteps = sensorSteps, updatedAt = System.currentTimeMillis())
            ?: StepDayEntity(date = date, sensorSteps = sensorSteps, updatedAt = System.currentTimeMillis())
        stepDao.insertOrUpdateStepDay(updated)
    }

    override suspend fun updateHealthConnectSteps(date: String, healthConnectSteps: Int) {
        val existing = stepDao.getStepDaySync(date)
        val updated = existing?.copy(healthConnectSteps = healthConnectSteps, updatedAt = System.currentTimeMillis())
            ?: StepDayEntity(date = date, healthConnectSteps = healthConnectSteps, updatedAt = System.currentTimeMillis())
        stepDao.insertOrUpdateStepDay(updated)
    }

    override suspend fun setManualSteps(date: String, manualSteps: Int?) {
        val existing = stepDao.getStepDaySync(date)
        val updated = existing?.copy(manualSteps = manualSteps, updatedAt = System.currentTimeMillis())
            ?: StepDayEntity(date = date, manualSteps = manualSteps, updatedAt = System.currentTimeMillis())
        stepDao.insertOrUpdateStepDay(updated)
    }

    override suspend fun setStepSource(date: String, source: StepSource) {
        val existing = stepDao.getStepDaySync(date)
        val updated = existing?.copy(source = source.name, updatedAt = System.currentTimeMillis())
            ?: StepDayEntity(date = date, source = source.name, updatedAt = System.currentTimeMillis())
        stepDao.insertOrUpdateStepDay(updated)
    }

    override suspend fun recordSensorSnapshot(counterValue: Long) {
        val snapshot = StepSnapshotEntity(
            timestamp = System.currentTimeMillis(),
            counterValue = counterValue
        )
        stepDao.insertSnapshot(snapshot)
    }
}
