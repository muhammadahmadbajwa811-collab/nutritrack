package com.nutritrack.app.core.health.di

import com.nutritrack.app.core.health.StepTracker
import com.nutritrack.app.core.health.StepTrackerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthModule {

    @Binds
    @Singleton
    abstract fun bindStepTracker(impl: StepTrackerImpl): StepTracker
}
