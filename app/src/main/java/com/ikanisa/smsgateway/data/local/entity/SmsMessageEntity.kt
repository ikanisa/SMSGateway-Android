package com.ikanisa.smsgateway.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ikanisa.smsgateway.domain.model.SyncStatus
import java.util.UUID

/**
 * Room entity for storing SMS messages locally.
 * 
 * Includes indices for:
 * - message_hash: Unique constraint for deduplication
 * - sender: Quick lookups by sender
 * - received_at: Sorting by received time
 * - sync_status: Filtering by sync state
 */
@Entity(
    tableName = "sms_messages",
    indices = [
        Index(value = ["message_hash"], unique = true),
        Index(value = ["sender"]),
        Index(value = ["received_at"]),
        Index(value = ["sync_status"])
    ]
)
data class SmsMessageEntity(
    /** Unique identifier for the message */
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    /** Phone number or name of the sender */
    @ColumnInfo(name = "sender")
    val sender: String,
    
    /** SMS message content/body */
    @ColumnInfo(name = "content")
    val content: String,
    
    /** Timestamp when SMS was received (millis since epoch) */
    @ColumnInfo(name = "received_at")
    val receivedAt: Long,
    
    /** SHA-256 hash of sender + content + timestamp for deduplication */
    @ColumnInfo(name = "message_hash")
    val messageHash: String,
    
    /** Current synchronization status */
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    
    /** Number of sync attempts (for retry backoff) */
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    
    /** Last error message if sync failed */
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,
    
    /** Timestamp when entity was created in local DB */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    /** Timestamp when message was successfully synced (null if not synced) */
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long? = null,
    
    /** SIM slot index if available */
    @ColumnInfo(name = "sim_slot")
    val simSlot: Int? = null
)
