package com.ikanisa.smsgateway.di

import android.content.Context
import com.ikanisa.smsgateway.data.local.dao.AuditDao
import com.ikanisa.smsgateway.data.local.database.AppDatabase
import com.ikanisa.smsgateway.data.remote.interceptor.CertificatePinningConfig
import com.ikanisa.smsgateway.data.remote.interceptor.HmacInterceptor
import com.ikanisa.smsgateway.data.security.SecurePreferences
import com.ikanisa.smsgateway.util.AuditLogger
import com.ikanisa.smsgateway.util.BiometricAuthManager
import com.ikanisa.smsgateway.util.SecurityChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier for secured OkHttpClient with certificate pinning.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SecuredHttpClient

/**
 * Hilt module for security-related dependencies.
 * 
 * Provides:
 * - SecurePreferences for encrypted storage
 * - SecurityChecker for device integrity checks
 * - BiometricAuthManager for biometric authentication
 * - AuditLogger for security event logging
 * - Secured OkHttpClient with certificate pinning and HMAC signing
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    
    @Provides
    @Singleton
    fun provideSecurePreferences(
        @ApplicationContext context: Context
    ): SecurePreferences {
        return SecurePreferences(context)
    }
    
    @Provides
    @Singleton
    fun provideSecurityChecker(
        @ApplicationContext context: Context
    ): SecurityChecker {
        return SecurityChecker(context)
    }
    
    @Provides
    @Singleton
    fun provideBiometricAuthManager(
        @ApplicationContext context: Context,
        securePreferences: SecurePreferences
    ): BiometricAuthManager {
        return BiometricAuthManager(context, securePreferences)
    }
    
    @Provides
    @Singleton
    fun provideAuditDao(database: AppDatabase): AuditDao {
        return database.auditDao()
    }
    
    @Provides
    @Singleton
    fun provideAuditLogger(auditDao: AuditDao): AuditLogger {
        return AuditLogger(auditDao)
    }
    
    @Provides
    @Singleton
    fun provideCertificatePinningConfig(
        @ApplicationContext context: Context
    ): CertificatePinningConfig {
        return CertificatePinningConfig(context)
    }
    
    @Provides
    @Singleton
    fun provideHmacInterceptor(
        securePreferences: SecurePreferences
    ): HmacInterceptor {
        return HmacInterceptor(securePreferences)
    }
    
    @Provides
    @Singleton
    @SecuredHttpClient
    fun provideSecuredOkHttpClient(
        certificatePinningConfig: CertificatePinningConfig,
        hmacInterceptor: HmacInterceptor
    ): OkHttpClient {
        return certificatePinningConfig.createSecureOkHttpClient()
            .newBuilder()
            .addInterceptor(hmacInterceptor)
            .build()
    }
}
