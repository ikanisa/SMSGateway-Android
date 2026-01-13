package com.ikanisa.smsgateway.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ikanisa.smsgateway.data.local.dao.AuditDao
import com.ikanisa.smsgateway.data.local.dao.DeviceDao
import com.ikanisa.smsgateway.data.local.dao.SmsDao
import com.ikanisa.smsgateway.data.local.dao.SyncQueueDao
import com.ikanisa.smsgateway.data.local.dao.TransactionDao
import com.ikanisa.smsgateway.data.local.entity.AuditLogEntity
import com.ikanisa.smsgateway.data.local.entity.DeviceEntity
import com.ikanisa.smsgateway.data.local.entity.SmsMessageEntity
import com.ikanisa.smsgateway.data.local.entity.SyncQueueEntity
import com.ikanisa.smsgateway.data.local.entity.TransactionEntity

/**
 * Room database for the SMS Gateway application.
 * 
 * Contains tables for:
 * - SMS messages with sync status
 * - Device configuration
 * - Parsed transactions
 * - Sync retry queue
 * - Security audit logs
 * 
 * Uses SQLCipher for database encryption when configured via DatabaseModule.
 */
@Database(
    entities = [
        SmsMessageEntity::class,
        DeviceEntity::class,
        TransactionEntity::class,
        SyncQueueEntity::class,
        AuditLogEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun smsDao(): SmsDao
    abstract fun deviceDao(): DeviceDao
    abstract fun transactionDao(): TransactionDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun auditDao(): AuditDao
    
    companion object {
        const val DATABASE_NAME = "sms_gateway.db"
        
        /**
         * Create an unencrypted database instance (for testing or development).
         */
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
        
        /**
         * Create an in-memory database for testing.
         */
        fun createInMemory(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}
