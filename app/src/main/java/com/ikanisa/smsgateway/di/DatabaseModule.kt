package com.ikanisa.smsgateway.di

import android.content.Context
import androidx.room.Room
import com.ikanisa.smsgateway.data.local.dao.DeviceDao
import com.ikanisa.smsgateway.data.local.dao.SmsDao
import com.ikanisa.smsgateway.data.local.dao.SyncQueueDao
import com.ikanisa.smsgateway.data.local.dao.TransactionDao
import com.ikanisa.smsgateway.data.local.database.AppDatabase
import com.ikanisa.smsgateway.data.security.SecurePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import timber.log.Timber
import javax.inject.Singleton

/**
 * Hilt module for database and DAO dependencies.
 * 
 * Provides encrypted Room database using SQLCipher.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideSecurePreferences(
        @ApplicationContext context: Context
    ): SecurePreferences {
        return SecurePreferences(context)
    }
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        securePreferences: SecurePreferences
    ): AppDatabase {
        val passphrase = securePreferences.getDatabaseKey()
        val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray()))
        
        Timber.d("Initializing encrypted Room database")
        
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
            .also { Timber.i("AppDatabase initialized successfully") }
    }
    
    @Provides
    fun provideSmsDao(database: AppDatabase): SmsDao {
        return database.smsDao()
    }
    
    @Provides
    fun provideDeviceDao(database: AppDatabase): DeviceDao {
        return database.deviceDao()
    }
    
    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    @Provides
    fun provideSyncQueueDao(database: AppDatabase): SyncQueueDao {
        return database.syncQueueDao()
    }
}
