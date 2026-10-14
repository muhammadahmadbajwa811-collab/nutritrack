package com.nutritrack.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nutritrack.app.core.database.dao.FoodDao
import com.nutritrack.app.core.database.entity.FoodItemEntity

@Database(
    entities = [FoodItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NutriTrackDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao

    companion object {
        const val DATABASE_NAME = "nutritrack.db"
    }
}
