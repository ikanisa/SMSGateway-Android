package com.example.smsgateway.di

import android.content.Context
import com.example.smsgateway.data.SecurePreferences
import com.example.smsgateway.data.repository.SmsRepository
import com.example.smsgateway.data.repository.SmsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideSecurePreferences(
        @ApplicationContext context: Context
    ): SecurePreferences {
        return SecurePreferences(context)
    }
    
    @Provides
    @Singleton
    fun provideSmsRepository(
        @ApplicationContext context: Context
    ): SmsRepository {
        return SmsRepositoryImpl(context)
    }
}
