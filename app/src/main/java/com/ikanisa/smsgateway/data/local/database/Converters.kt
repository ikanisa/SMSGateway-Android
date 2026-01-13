package com.ikanisa.smsgateway.data.local.database

import androidx.room.TypeConverter
import com.ikanisa.smsgateway.domain.model.SyncStatus

/**
 * Room type converters for non-primitive types.
 */
class Converters {
    
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return try {
            SyncStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            SyncStatus.PENDING
        }
    }
}
