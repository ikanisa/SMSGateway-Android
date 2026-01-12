package com.ikanisa.smsgateway.di

import android.content.Context
import androidx.work.WorkManager
import com.ikanisa.smsgateway.data.datasource.NotificationApi
import com.ikanisa.smsgateway.data.repository.NotificationRepository
import com.ikanisa.smsgateway.data.repository.NotificationRepositoryImpl
import com.ikanisa.smsgateway.data.repository.SmsRepository
import com.ikanisa.smsgateway.data.repository.SmsRepositoryImpl
import com.ikanisa.smsgateway.notification.NotificationScheduler
import com.ikanisa.smsgateway.notification.SmsNotificationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repositories and services.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideSmsRepository(): SmsRepository {
        return SmsRepositoryImpl()
    }
    
    @Provides
    @Singleton
    fun provideNotificationApi(): NotificationApi {
        return NotificationApi()
    }
    
    @Provides
    @Singleton
    fun provideSmsNotificationService(
        @ApplicationContext context: Context
    ): SmsNotificationService {
        return SmsNotificationService(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationRepository(
        notificationApi: NotificationApi,
        smsNotificationService: SmsNotificationService
    ): NotificationRepository {
        return NotificationRepositoryImpl(notificationApi, smsNotificationService)
    }
    
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationScheduler(
        workManager: WorkManager
    ): NotificationScheduler {
        return NotificationScheduler(workManager)
    }
    
    @Provides
    @Singleton
    fun provideFcmTokenRegistrationService(): com.ikanisa.smsgateway.notification.FcmTokenRegistrationService {
        return com.ikanisa.smsgateway.notification.FcmTokenRegistrationService()
    }
    
    @Provides
    @Singleton
    fun provideActivationRepository(
        @ApplicationContext context: Context,
        supabaseClient: io.github.jan.supabase.SupabaseClient
    ): com.ikanisa.smsgateway.data.repository.ActivationRepository {
        return com.ikanisa.smsgateway.data.repository.ActivationRepositoryImpl(context, supabaseClient)
    }
}
