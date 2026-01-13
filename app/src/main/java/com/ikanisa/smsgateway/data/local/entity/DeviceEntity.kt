package com.ikanisa.smsgateway.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing device configuration.
 * 
 * Stores MOMO code, device identification, and activation status.
 * Only one device configuration is expected (primary key is always "default").
 */
@Entity(tableName = "device_config")
data class DeviceEntity(
    /** Fixed primary key - only one device config per app */
    @PrimaryKey
    val id: String = "default",
    
    /** MOMO activation code for this device */
    @ColumnInfo(name = "momo_code")
    val momoCode: String,
    
    /** Phone number associated with this device */
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String? = null,
    
    /** Device nickname/label for identification */
    @ColumnInfo(name = "device_name")
    val deviceName: String? = null,
    
    /** Whether device is activated and authorized */
    @ColumnInfo(name = "is_activated")
    val isActivated: Boolean = false,
    
    /** FCM token for push notifications */
    @ColumnInfo(name = "fcm_token")
    val fcmToken: String? = null,
    
    /** Timestamp when device was activated */
    @ColumnInfo(name = "activated_at")
    val activatedAt: Long? = null,
    
    /** Last successful sync timestamp */
    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Long? = null,
    
    /** Timestamp when config was created */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    /** Timestamp when config was last updated */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
