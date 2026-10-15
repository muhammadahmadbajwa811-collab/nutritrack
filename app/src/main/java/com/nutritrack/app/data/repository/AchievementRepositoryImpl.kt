package com.nutritrack.app.data.repository

import com.nutritrack.app.core.database.dao.AchievementDao
import com.nutritrack.app.core.database.entity.AchievementEntity
import com.nutritrack.app.data.mapper.toDomainModel
import com.nutritrack.app.domain.model.Achievement
import com.nutritrack.app.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepositoryImpl @Inject constructor(
    private val achievementDao: AchievementDao
) : AchievementRepository {

    override fun getAllAchievements(): Flow<List<Achievement>> {
        return achievementDao.getAllAchievements().map { list -> list.map { it.toDomainModel() } }
    }

    override fun getAchievement(key: String): Flow<Achievement?> {
        return achievementDao.getAchievement(key).map { it?.toDomainModel() }
    }

    override fun getUnlockedCount(): Flow<Int> {
        return achievementDao.getUnlockedCount()
    }

    override suspend fun updateAchievementProgress(key: String, progress: Int, unlockedAt: Long?) {
        val entity = AchievementEntity(
            key = key,
            unlockedAt = unlockedAt,
            progress = progress
        )
        achievementDao.insertOrUpdateAchievement(entity)
    }

    override suspend fun unlockAchievement(key: String, timestamp: Long) {
        val entity = AchievementEntity(
            key = key,
            unlockedAt = timestamp,
            progress = 1
        )
        achievementDao.insertOrUpdateAchievement(entity)
    }
}
