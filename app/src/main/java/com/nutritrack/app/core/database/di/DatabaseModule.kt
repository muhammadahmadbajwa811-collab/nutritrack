package com.nutritrack.app.core.database.di

import android.content.Context
import androidx.room.Room
import com.nutritrack.app.core.database.NutriTrackDatabase
import com.nutritrack.app.core.database.dao.FoodDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNutriTrackDatabase(
        @ApplicationContext context: Context
    ): NutriTrackDatabase {
        return Room.databaseBuilder(
            context,
            NutriTrackDatabase::class.java,
            NutriTrackDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideFoodDao(database: NutriTrackDatabase): FoodDao {
        return database.foodDao()
    }
}
