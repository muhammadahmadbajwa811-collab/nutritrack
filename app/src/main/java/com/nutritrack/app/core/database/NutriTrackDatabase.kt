package com.nutritrack.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nutritrack.app.core.database.dao.AchievementDao
import com.nutritrack.app.core.database.dao.AppMetaDao
import com.nutritrack.app.core.database.dao.DiaryDao
import com.nutritrack.app.core.database.dao.FoodDao
import com.nutritrack.app.core.database.dao.StepDao
import com.nutritrack.app.core.database.dao.UserProfileDao
import com.nutritrack.app.core.database.dao.WaterDao
import com.nutritrack.app.core.database.dao.WeightDao
import com.nutritrack.app.core.database.dao.WorkoutDao
import com.nutritrack.app.core.database.entity.AchievementEntity
import com.nutritrack.app.core.database.entity.AppMetaEntity
import com.nutritrack.app.core.database.entity.DiaryEntryEntity
import com.nutritrack.app.core.database.entity.FoodEntity
import com.nutritrack.app.core.database.entity.FoodFtsEntity
import com.nutritrack.app.core.database.entity.ServingOptionEntity
import com.nutritrack.app.core.database.entity.StepDayEntity
import com.nutritrack.app.core.database.entity.StepSnapshotEntity
import com.nutritrack.app.core.database.entity.UserProfileEntity
import com.nutritrack.app.core.database.entity.WaterLogEntity
import com.nutritrack.app.core.database.entity.WeightLogEntity
import com.nutritrack.app.core.database.entity.WorkoutEntity

@Database(
    entities = [
        UserProfileEntity::class,
        FoodEntity::class,
        FoodFtsEntity::class,
        ServingOptionEntity::class,
        DiaryEntryEntity::class,
        WaterLogEntity::class,
        StepDayEntity::class,
        StepSnapshotEntity::class,
        WorkoutEntity::class,
        WeightLogEntity::class,
        AchievementEntity::class,
        AppMetaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NutriTrackDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun foodDao(): FoodDao
    abstract fun diaryDao(): DiaryDao
    abstract fun waterDao(): WaterDao
    abstract fun stepDao(): StepDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun weightDao(): WeightDao
    abstract fun achievementDao(): AchievementDao
    abstract fun appMetaDao(): AppMetaDao

    companion object {
        const val DATABASE_NAME = "nutritrack.db"
    }
}
