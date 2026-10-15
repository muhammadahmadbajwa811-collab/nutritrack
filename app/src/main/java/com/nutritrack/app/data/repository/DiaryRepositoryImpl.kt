package com.nutritrack.app.data.repository

import com.nutritrack.app.core.database.dao.DiaryDao
import com.nutritrack.app.data.mapper.toDomainModel
import com.nutritrack.app.data.mapper.toEntity
import com.nutritrack.app.domain.model.DiaryEntry
import com.nutritrack.app.domain.model.MealType
import com.nutritrack.app.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepositoryImpl @Inject constructor(
    private val diaryDao: DiaryDao
) : DiaryRepository {

    override fun getEntriesForDate(date: String): Flow<List<DiaryEntry>> {
        return diaryDao.getEntriesForDate(date).map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun getEntriesForDateSync(date: String): List<DiaryEntry> {
        return diaryDao.getEntriesForDateSync(date).map { it.toDomainModel() }
    }

    override fun getEntriesForDateAndMeal(date: String, meal: MealType): Flow<List<DiaryEntry>> {
        return diaryDao.getEntriesForDateAndMeal(date, meal.name).map { list -> list.map { it.toDomainModel() } }
    }

    override fun getEntriesBetweenDates(startDate: String, endDate: String): Flow<List<DiaryEntry>> {
        return diaryDao.getEntriesBetweenDates(startDate, endDate).map { list -> list.map { it.toDomainModel() } }
    }

    override fun getAllLoggedDates(): Flow<List<String>> {
        return diaryDao.getAllLoggedDates()
    }

    override fun getCaloriesForDate(date: String): Flow<Double> {
        return diaryDao.getCaloriesForDate(date).map { it ?: 0.0 }
    }

    override fun getProteinForDate(date: String): Flow<Double> {
        return diaryDao.getProteinForDate(date).map { it ?: 0.0 }
    }

    override fun getCarbsForDate(date: String): Flow<Double> {
        return diaryDao.getCarbsForDate(date).map { it ?: 0.0 }
    }

    override fun getFatForDate(date: String): Flow<Double> {
        return diaryDao.getFatForDate(date).map { it ?: 0.0 }
    }

    override suspend fun addEntry(entry: DiaryEntry) {
        diaryDao.insertEntry(entry.toEntity())
    }

    override suspend fun addEntries(entries: List<DiaryEntry>) {
        diaryDao.insertEntries(entries.map { it.toEntity() })
    }

    override suspend fun updateEntry(entry: DiaryEntry) {
        diaryDao.updateEntry(entry.toEntity())
    }

    override suspend fun deleteEntry(id: String) {
        diaryDao.deleteEntryById(id)
    }

    override suspend fun clearDate(date: String) {
        diaryDao.deleteEntriesForDate(date)
    }
}
