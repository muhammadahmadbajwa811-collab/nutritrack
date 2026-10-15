package com.nutritrack.app.domain.repository

import com.nutritrack.app.domain.model.Achievement
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun getAllAchievements(): Flow<List<Achievement>>
    fun getAchievement(key: String): Flow<Achievement?>
    fun getUnlockedCount(): Flow<Int>
    suspend fun updateAchievementProgress(key: String, progress: Int, unlockedAt: Long? = null)
    suspend fun unlockAchievement(key: String, timestamp: Long = System.currentTimeMillis())
}
