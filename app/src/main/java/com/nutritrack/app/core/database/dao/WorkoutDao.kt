package com.nutritrack.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nutritrack.app.core.database.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE date = :date ORDER BY createdAt ASC")
    fun getWorkoutsForDate(date: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE date = :date ORDER BY createdAt ASC")
    suspend fun getWorkoutsForDateSync(date: String): List<WorkoutEntity>

    @Query("SELECT SUM(calories) FROM workouts WHERE date = :date")
    fun getTotalCaloriesForDate(date: String): Flow<Double?>

    @Query("SELECT SUM(minutes) FROM workouts WHERE date = :date")
    fun getTotalMinutesForDate(date: String): Flow<Int?>

    @Query("SELECT * FROM workouts WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, createdAt ASC")
    fun getWorkoutsBetweenDates(startDate: String, endDate: String): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkoutById(id: String)

    @Query("DELETE FROM workouts WHERE date = :date")
    suspend fun clearDate(date: String)
}
