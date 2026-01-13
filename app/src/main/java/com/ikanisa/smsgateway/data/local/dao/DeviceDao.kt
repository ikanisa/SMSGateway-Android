package com.ikanisa.smsgateway.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ikanisa.smsgateway.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for device configuration.
 * 
 * Only one device config is expected per app installation.
 */
@Dao
interface DeviceDao {
    
    /**
     * Get the current device configuration.
     */
    @Query("SELECT * FROM device_config WHERE id = 'default' LIMIT 1")
    suspend fun getDevice(): DeviceEntity?
    
    /**
     * Observe device configuration changes.
     */
    @Query("SELECT * FROM device_config WHERE id = 'default' LIMIT 1")
    fun getDeviceFlow(): Flow<DeviceEntity?>
    
    /**
     * Check if device is activated.
     */
    @Query("SELECT is_activated FROM device_config WHERE id = 'default' LIMIT 1")
    suspend fun isActivated(): Boolean?
    
    /**
     * Insert or replace device configuration.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(device: DeviceEntity)
    
    /**
     * Update device configuration.
     */
    @Update
    suspend fun update(device: DeviceEntity)
    
    /**
     * Update MOMO code.
     */
    @Query("""
        UPDATE device_config 
        SET momo_code = :momoCode, updated_at = :timestamp 
        WHERE id = 'default'
    """)
    suspend fun updateMomoCode(momoCode: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Update FCM token.
     */
    @Query("""
        UPDATE device_config 
        SET fcm_token = :token, updated_at = :timestamp 
        WHERE id = 'default'
    """)
    suspend fun updateFcmToken(token: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Mark device as activated.
     */
    @Query("""
        UPDATE device_config 
        SET is_activated = 1, activated_at = :timestamp, updated_at = :timestamp 
        WHERE id = 'default'
    """)
    suspend fun activate(timestamp: Long = System.currentTimeMillis())
    
    /**
     * Update last sync timestamp.
     */
    @Query("""
        UPDATE device_config 
        SET last_sync_at = :timestamp, updated_at = :timestamp 
        WHERE id = 'default'
    """)
    suspend fun updateLastSync(timestamp: Long = System.currentTimeMillis())
    
    /**
     * Delete device configuration.
     */
    @Query("DELETE FROM device_config")
    suspend fun delete()
}
