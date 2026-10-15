package com.nutritrack.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nutritrack.app.core.database.entity.WeightLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_logs WHERE date = :date LIMIT 1")
    fun getWeightForDate(date: String): Flow<WeightLogEntity?>

    @Query("SELECT * FROM weight_logs WHERE date = :date LIMIT 1")
    suspend fun getWeightForDateSync(date: String): WeightLogEntity?

    @Query("SELECT * FROM weight_logs ORDER BY date DESC LIMIT 1")
    fun getLatestWeight(): Flow<WeightLogEntity?>

    @Query("SELECT * FROM weight_logs ORDER BY date DESC LIMIT 1")
    suspend fun getLatestWeightSync(): WeightLogEntity?

    @Query("SELECT * FROM weight_logs ORDER BY date ASC")
    fun getAllWeights(): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs ORDER BY date ASC")
    suspend fun getAllWeightsSync(): List<WeightLogEntity>

    @Query("SELECT * FROM weight_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getWeightsBetweenDates(startDate: String, endDate: String): Flow<List<WeightLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWeight(weightLog: WeightLogEntity)

    @Query("DELETE FROM weight_logs WHERE date = :date")
    suspend fun deleteWeightForDate(date: String)

    @Query("DELETE FROM weight_logs")
    suspend fun clearAll()
}
