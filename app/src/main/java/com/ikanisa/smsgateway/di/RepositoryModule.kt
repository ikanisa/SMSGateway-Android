package com.ikanisa.smsgateway.di

import com.ikanisa.smsgateway.data.repository.SmsRepository
import com.ikanisa.smsgateway.data.repository.SmsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Simplified Hilt module for repository.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideSmsRepository(): SmsRepository {
        return SmsRepositoryImpl()
    }
}
