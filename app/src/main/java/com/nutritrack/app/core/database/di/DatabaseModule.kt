package com.nutritrack.app.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nutritrack.app.core.database.NutriTrackDatabase
import com.nutritrack.app.core.database.dao.AchievementDao
import com.nutritrack.app.core.database.dao.AppMetaDao
import com.nutritrack.app.core.database.dao.DiaryDao
import com.nutritrack.app.core.database.dao.FoodDao
import com.nutritrack.app.core.database.dao.StepDao
import com.nutritrack.app.core.database.dao.UserProfileDao
import com.nutritrack.app.core.database.dao.WaterDao
import com.nutritrack.app.core.database.dao.WeightDao
import com.nutritrack.app.core.database.dao.WorkoutDao
import com.nutritrack.app.core.database.seed.SeedDataLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNutriTrackDatabase(
        @ApplicationContext context: Context
    ): NutriTrackDatabase {
        lateinit var database: NutriTrackDatabase
        val callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    SeedDataLoader.loadSeedFoodsIfNeeded(context, database)
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                CoroutineScope(Dispatchers.IO).launch {
                    SeedDataLoader.loadSeedFoodsIfNeeded(context, database)
                }
            }
        }

        database = Room.databaseBuilder(
            context,
            NutriTrackDatabase::class.java,
            NutriTrackDatabase.DATABASE_NAME
        )
        .addCallback(callback)
        .fallbackToDestructiveMigration()
        .build()

        return database
    }

    @Provides
    fun provideUserProfileDao(database: NutriTrackDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    fun provideFoodDao(database: NutriTrackDatabase): FoodDao = database.foodDao()

    @Provides
    fun provideDiaryDao(database: NutriTrackDatabase): DiaryDao = database.diaryDao()

    @Provides
    fun provideWaterDao(database: NutriTrackDatabase): WaterDao = database.waterDao()

    @Provides
    fun provideStepDao(database: NutriTrackDatabase): StepDao = database.stepDao()

    @Provides
    fun provideWorkoutDao(database: NutriTrackDatabase): WorkoutDao = database.workoutDao()

    @Provides
    fun provideWeightDao(database: NutriTrackDatabase): WeightDao = database.weightDao()

    @Provides
    fun provideAchievementDao(database: NutriTrackDatabase): AchievementDao = database.achievementDao()

    @Provides
    fun provideAppMetaDao(database: NutriTrackDatabase): AppMetaDao = database.appMetaDao()
}
