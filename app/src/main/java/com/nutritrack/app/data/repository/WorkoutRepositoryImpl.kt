package com.nutritrack.app.data.repository

import com.nutritrack.app.core.database.dao.WorkoutDao
import com.nutritrack.app.data.mapper.toDomainModel
import com.nutritrack.app.data.mapper.toEntity
import com.nutritrack.app.domain.model.WorkoutEntry
import com.nutritrack.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {

    override fun getWorkoutsForDate(date: String): Flow<List<WorkoutEntry>> {
        return workoutDao.getWorkoutsForDate(date).map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun getWorkoutsForDateSync(date: String): List<WorkoutEntry> {
        return workoutDao.getWorkoutsForDateSync(date).map { it.toDomainModel() }
    }

    override fun getTotalWorkoutCalories(date: String): Flow<Double> {
        return workoutDao.getTotalCaloriesForDate(date).map { it ?: 0.0 }
    }

    override fun getTotalWorkoutMinutes(date: String): Flow<Int> {
        return workoutDao.getTotalMinutesForDate(date).map { it ?: 0 }
    }

    override fun getWorkoutsBetweenDates(startDate: String, endDate: String): Flow<List<WorkoutEntry>> {
        return workoutDao.getWorkoutsBetweenDates(startDate, endDate).map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun logWorkout(workout: WorkoutEntry) {
        workoutDao.insertWorkout(workout.toEntity())
    }

    override suspend fun deleteWorkout(id: String) {
        workoutDao.deleteWorkoutById(id)
    }
}
