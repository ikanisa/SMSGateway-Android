package com.ikanisa.smsgateway.di

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for WorkManager dependencies.
 * 
 * Note: WorkManager instance is provided by RepositoryModule.
 * This module provides additional WorkManager-related dependencies
 * and configuration utilities.
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    
    /**
     * Provides WorkManager configuration builder.
     * Useful for testing or custom configuration needs.
     */
    @Provides
    fun provideWorkManagerConfiguration(
        workerFactory: HiltWorkerFactory
    ): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
}
