package com.nutritrack.app.data.di

import com.nutritrack.app.data.repository.AchievementRepositoryImpl
import com.nutritrack.app.data.repository.DiaryRepositoryImpl
import com.nutritrack.app.data.repository.FoodRepositoryImpl
import com.nutritrack.app.data.repository.StepRepositoryImpl
import com.nutritrack.app.data.repository.UserRepositoryImpl
import com.nutritrack.app.data.repository.WaterRepositoryImpl
import com.nutritrack.app.data.repository.WeightRepositoryImpl
import com.nutritrack.app.data.repository.WorkoutRepositoryImpl
import com.nutritrack.app.domain.repository.AchievementRepository
import com.nutritrack.app.domain.repository.DiaryRepository
import com.nutritrack.app.domain.repository.FoodRepository
import com.nutritrack.app.domain.repository.StepRepository
import com.nutritrack.app.domain.repository.UserRepository
import com.nutritrack.app.domain.repository.WaterRepository
import com.nutritrack.app.domain.repository.WeightRepository
import com.nutritrack.app.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFoodRepository(impl: FoodRepositoryImpl): FoodRepository

    @Binds
    @Singleton
    abstract fun bindDiaryRepository(impl: DiaryRepositoryImpl): DiaryRepository

    @Binds
    @Singleton
    abstract fun bindWaterRepository(impl: WaterRepositoryImpl): WaterRepository

    @Binds
    @Singleton
    abstract fun bindStepRepository(impl: StepRepositoryImpl): StepRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindWeightRepository(impl: WeightRepositoryImpl): WeightRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindAchievementRepository(impl: AchievementRepositoryImpl): AchievementRepository
}
