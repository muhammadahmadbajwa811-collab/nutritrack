package com.nutritrack.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nutritrack.app.core.database.entity.StepDayEntity
import com.nutritrack.app.core.database.entity.StepSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM step_days WHERE date = :date LIMIT 1")
    fun getStepDay(date: String): Flow<StepDayEntity?>

    @Query("SELECT * FROM step_days WHERE date = :date LIMIT 1")
    suspend fun getStepDaySync(date: String): StepDayEntity?

    @Query("SELECT * FROM step_days WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getStepsBetweenDates(startDate: String, endDate: String): Flow<List<StepDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStepDay(stepDay: StepDayEntity)

    // Snapshots
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: StepSnapshotEntity)

    @Query("SELECT * FROM step_snapshots ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSnapshot(): StepSnapshotEntity?

    @Query("DELETE FROM step_snapshots WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteSnapshotsOlderThan(cutoffTimestamp: Long)
}
