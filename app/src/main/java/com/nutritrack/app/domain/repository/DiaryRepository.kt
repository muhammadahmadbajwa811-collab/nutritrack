package com.nutritrack.app.domain.repository

import com.nutritrack.app.domain.model.DiaryEntry
import com.nutritrack.app.domain.model.MealType
import kotlinx.coroutines.flow.Flow

interface DiaryRepository {
    fun getEntriesForDate(date: String): Flow<List<DiaryEntry>>
    suspend fun getEntriesForDateSync(date: String): List<DiaryEntry>
    fun getEntriesForDateAndMeal(date: String, meal: MealType): Flow<List<DiaryEntry>>
    fun getEntriesBetweenDates(startDate: String, endDate: String): Flow<List<DiaryEntry>>
    fun getAllLoggedDates(): Flow<List<String>>
    fun getCaloriesForDate(date: String): Flow<Double>
    fun getProteinForDate(date: String): Flow<Double>
    fun getCarbsForDate(date: String): Flow<Double>
    fun getFatForDate(date: String): Flow<Double>
    suspend fun addEntry(entry: DiaryEntry)
    suspend fun addEntries(entries: List<DiaryEntry>)
    suspend fun updateEntry(entry: DiaryEntry)
    suspend fun deleteEntry(id: String)
    suspend fun clearDate(date: String)
}
