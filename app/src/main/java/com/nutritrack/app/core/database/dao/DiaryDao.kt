package com.nutritrack.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nutritrack.app.core.database.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries WHERE date = :date ORDER BY createdAt ASC")
    fun getEntriesForDate(date: String): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE date = :date ORDER BY createdAt ASC")
    suspend fun getEntriesForDateSync(date: String): List<DiaryEntryEntity>

    @Query("SELECT * FROM diary_entries WHERE date = :date AND meal = :meal ORDER BY createdAt ASC")
    fun getEntriesForDateAndMeal(date: String, meal: String): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, createdAt ASC")
    fun getEntriesBetweenDates(startDate: String, endDate: String): Flow<List<DiaryEntryEntity>>

    @Query("SELECT DISTINCT date FROM diary_entries ORDER BY date DESC")
    fun getAllLoggedDates(): Flow<List<String>>

    @Query("SELECT DISTINCT date FROM diary_entries ORDER BY date DESC")
    suspend fun getAllLoggedDatesSync(): List<String>

    @Query("SELECT SUM(calories) FROM diary_entries WHERE date = :date")
    fun getCaloriesForDate(date: String): Flow<Double?>

    @Query("SELECT SUM(protein) FROM diary_entries WHERE date = :date")
    fun getProteinForDate(date: String): Flow<Double?>

    @Query("SELECT SUM(carbs) FROM diary_entries WHERE date = :date")
    fun getCarbsForDate(date: String): Flow<Double?>

    @Query("SELECT SUM(fat) FROM diary_entries WHERE date = :date")
    fun getFatForDate(date: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<DiaryEntryEntity>)

    @Update
    suspend fun updateEntry(entry: DiaryEntryEntity)

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteEntryById(id: String)

    @Query("DELETE FROM diary_entries WHERE date = :date")
    suspend fun deleteEntriesForDate(date: String)

    @Query("DELETE FROM diary_entries")
    suspend fun clearAll()
}
