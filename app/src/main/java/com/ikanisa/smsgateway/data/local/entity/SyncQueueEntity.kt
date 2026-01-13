package com.ikanisa.smsgateway.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity for managing failed sync retry queue.
 * 
 * Tracks messages that need to be re-synced with exponential backoff.
 */
@Entity(
    tableName = "sync_queue",
    indices = [
        Index(value = ["sms_id"], unique = true),
        Index(value = ["next_retry_at"]),
        Index(value = ["priority"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = SmsMessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["sms_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SyncQueueEntity(
    /** Unique identifier for queue entry */
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    /** Reference to the SMS message to retry */
    @ColumnInfo(name = "sms_id")
    val smsId: String,
    
    /** Number of retry attempts made */
    @ColumnInfo(name = "attempt_count")
    val attemptCount: Int = 0,
    
    /** Timestamp for next retry attempt (millis since epoch) */
    @ColumnInfo(name = "next_retry_at")
    val nextRetryAt: Long,
    
    /** Priority level (lower = higher priority) */
    @ColumnInfo(name = "priority")
    val priority: Int = 0,
    
    /** Last error message from failed sync */
    @ColumnInfo(name = "last_error")
    val lastError: String? = null,
    
    /** HTTP status code from last failed request */
    @ColumnInfo(name = "last_status_code")
    val lastStatusCode: Int? = null,
    
    /** Timestamp when entry was created */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    /** Timestamp of last retry attempt */
    @ColumnInfo(name = "last_attempt_at")
    val lastAttemptAt: Long? = null
) {
    companion object {
        /** Maximum number of retry attempts before giving up */
        const val MAX_ATTEMPTS = 5
        
        /** Base delay in milliseconds for exponential backoff */
        const val BASE_DELAY_MS = 60_000L // 1 minute
        
        /**
         * Calculate next retry time using exponential backoff.
         * @param attemptCount Current attempt count (0-indexed)
         * @return Timestamp for next retry
         */
        fun calculateNextRetryTime(attemptCount: Int): Long {
            val delayMultiplier = (1 shl attemptCount.coerceAtMost(4)) // 1, 2, 4, 8, 16
            return System.currentTimeMillis() + (BASE_DELAY_MS * delayMultiplier)
        }
    }
}
