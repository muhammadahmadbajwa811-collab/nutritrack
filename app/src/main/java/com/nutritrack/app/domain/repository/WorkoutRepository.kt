package com.nutritrack.app.domain.repository

import com.nutritrack.app.domain.model.WorkoutEntry
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getWorkoutsForDate(date: String): Flow<List<WorkoutEntry>>
    suspend fun getWorkoutsForDateSync(date: String): List<WorkoutEntry>
    fun getTotalWorkoutCalories(date: String): Flow<Double>
    fun getTotalWorkoutMinutes(date: String): Flow<Int>
    fun getWorkoutsBetweenDates(startDate: String, endDate: String): Flow<List<WorkoutEntry>>
    suspend fun logWorkout(workout: WorkoutEntry)
    suspend fun deleteWorkout(id: String)
}
