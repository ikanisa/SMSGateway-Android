package com.ikanisa.smsgateway.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ikanisa.smsgateway.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the sync retry queue.
 * 
 * Manages messages that failed to sync and need retry with exponential backoff.
 */
@Dao
interface SyncQueueDao {
    
    /**
     * Get all queue entries ordered by priority and next retry time.
     */
    @Query("""
        SELECT * FROM sync_queue 
        ORDER BY priority ASC, next_retry_at ASC
    """)
    fun getAllFlow(): Flow<List<SyncQueueEntity>>
    
    /**
     * Get entries ready for retry (next_retry_at <= now).
     */
    @Query("""
        SELECT * FROM sync_queue 
        WHERE next_retry_at <= :now AND attempt_count < :maxAttempts
        ORDER BY priority ASC, next_retry_at ASC
        LIMIT :limit
    """)
    suspend fun getReadyForRetry(
        now: Long = System.currentTimeMillis(),
        maxAttempts: Int = 5,
        limit: Int = 50
    ): List<SyncQueueEntity>
    
    /**
     * Check if an SMS is in the retry queue.
     */
    @Query("SELECT * FROM sync_queue WHERE sms_id = :smsId LIMIT 1")
    suspend fun getBySmsId(smsId: String): SyncQueueEntity?
    
    /**
     * Get queue size.
     */
    @Query("SELECT COUNT(*) FROM sync_queue")
    fun getCount(): Flow<Int>
    
    /**
     * Get count of entries ready for retry.
     */
    @Query("SELECT COUNT(*) FROM sync_queue WHERE next_retry_at <= :now")
    suspend fun getReadyCount(now: Long = System.currentTimeMillis()): Int
    
    /**
     * Insert or update a queue entry.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: SyncQueueEntity)
    
    /**
     * Update an entry.
     */
    @Update
    suspend fun update(entry: SyncQueueEntity)
    
    /**
     * Increment attempt count and update retry time.
     */
    @Query("""
        UPDATE sync_queue 
        SET attempt_count = attempt_count + 1, 
            next_retry_at = :nextRetryAt,
            last_error = :error,
            last_status_code = :statusCode,
            last_attempt_at = :now
        WHERE id = :id
    """)
    suspend fun recordFailedAttempt(
        id: String,
        nextRetryAt: Long,
        error: String?,
        statusCode: Int?,
        now: Long = System.currentTimeMillis()
    )
    
    /**
     * Remove an entry by ID (after successful sync).
     */
    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteById(id: String)
    
    /**
     * Remove entry for a specific SMS.
     */
    @Query("DELETE FROM sync_queue WHERE sms_id = :smsId")
    suspend fun deleteBySmsId(smsId: String)
    
    /**
     * Remove entries that exceeded max attempts.
     */
    @Query("DELETE FROM sync_queue WHERE attempt_count >= :maxAttempts")
    suspend fun deleteExceededAttempts(maxAttempts: Int = 5)
    
    /**
     * Clear the entire queue.
     */
    @Query("DELETE FROM sync_queue")
    suspend fun deleteAll()
}
