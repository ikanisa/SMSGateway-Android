package com.ikanisa.smsgateway.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ikanisa.smsgateway.data.local.entity.SmsMessageEntity
import com.ikanisa.smsgateway.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for SMS messages.
 * 
 * Provides CRUD operations, sync status management, and deduplication logic.
 */
@Dao
interface SmsDao {
    
    // =========================================================================
    // Query Operations
    // =========================================================================
    
    /**
     * Observe all SMS messages ordered by received time (newest first).
     */
    @Query("SELECT * FROM sms_messages ORDER BY received_at DESC")
    fun getAllFlow(): Flow<List<SmsMessageEntity>>
    
    /**
     * Observe all SMS messages with a specific sync status.
     */
    @Query("SELECT * FROM sms_messages WHERE sync_status = :status ORDER BY created_at ASC")
    fun getByStatusFlow(status: SyncStatus): Flow<List<SmsMessageEntity>>
    
    /**
     * Get pending messages for sync (ordered by creation time, limited).
     */
    @Query("""
        SELECT * FROM sms_messages 
        WHERE sync_status = :status 
        ORDER BY created_at ASC 
        LIMIT :limit
    """)
    suspend fun getPendingSync(status: SyncStatus = SyncStatus.PENDING, limit: Int = 50): List<SmsMessageEntity>
    
    /**
     * Find a message by its content hash (for deduplication).
     */
    @Query("SELECT * FROM sms_messages WHERE message_hash = :hash LIMIT 1")
    suspend fun findByHash(hash: String): SmsMessageEntity?
    
    /**
     * Get a message by its ID.
     */
    @Query("SELECT * FROM sms_messages WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SmsMessageEntity?
    
    /**
     * Count messages by sync status.
     */
    @Query("SELECT COUNT(*) FROM sms_messages WHERE sync_status = :status")
    fun getCountByStatus(status: SyncStatus): Flow<Int>
    
    /**
     * Get total message count.
     */
    @Query("SELECT COUNT(*) FROM sms_messages")
    fun getTotalCount(): Flow<Int>
    
    // =========================================================================
    // Insert Operations
    // =========================================================================
    
    /**
     * Insert a new SMS message.
     * Uses IGNORE strategy to prevent duplicate hash entries.
     * 
     * @return Row ID if inserted, -1 if ignored (duplicate)
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sms: SmsMessageEntity): Long
    
    /**
     * Insert multiple SMS messages.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(messages: List<SmsMessageEntity>): List<Long>
    
    // =========================================================================
    // Update Operations
    // =========================================================================
    
    /**
     * Update an existing SMS message.
     */
    @Update
    suspend fun update(sms: SmsMessageEntity)
    
    /**
     * Update sync status for a message.
     */
    @Query("""
        UPDATE sms_messages 
        SET sync_status = :status, synced_at = :syncedAt 
        WHERE id = :id
    """)
    suspend fun updateSyncStatus(id: String, status: SyncStatus, syncedAt: Long?)
    
    /**
     * Increment retry count and set error message.
     */
    @Query("""
        UPDATE sms_messages 
        SET retry_count = retry_count + 1, 
            error_message = :error, 
            sync_status = :status 
        WHERE id = :id
    """)
    suspend fun incrementRetryCount(id: String, error: String, status: SyncStatus)
    
    /**
     * Mark a message as syncing.
     */
    @Query("UPDATE sms_messages SET sync_status = 'SYNCING' WHERE id = :id")
    suspend fun markSyncing(id: String)
    
    /**
     * Mark a message as synced with current timestamp.
     */
    @Query("""
        UPDATE sms_messages 
        SET sync_status = 'SYNCED', synced_at = :timestamp 
        WHERE id = :id
    """)
    suspend fun markSynced(id: String, timestamp: Long = System.currentTimeMillis())
    
    // =========================================================================
    // Delete Operations
    // =========================================================================
    
    /**
     * Delete old synced messages to free up space.
     * 
     * @param timestamp Delete messages synced before this time
     */
    @Query("DELETE FROM sms_messages WHERE synced_at < :timestamp")
    suspend fun deleteOldSynced(timestamp: Long)
    
    /**
     * Delete a message by ID.
     */
    @Query("DELETE FROM sms_messages WHERE id = :id")
    suspend fun deleteById(id: String)
    
    /**
     * Delete all messages (use with caution).
     */
    @Query("DELETE FROM sms_messages")
    suspend fun deleteAll()
    
    // =========================================================================
    // Transaction Operations
    // =========================================================================
    
    /**
     * Insert message with duplicate check.
     * 
     * @return true if inserted, false if duplicate exists
     */
    @Transaction
    suspend fun insertWithDuplicateCheck(sms: SmsMessageEntity): Boolean {
        return if (findByHash(sms.messageHash) == null) {
            insert(sms)
            true
        } else {
            false
        }
    }
    
    /**
     * Reset failed messages to pending for retry.
     */
    @Query("""
        UPDATE sms_messages 
        SET sync_status = 'PENDING', error_message = NULL 
        WHERE sync_status = 'FAILED' AND retry_count < :maxRetries
    """)
    suspend fun resetFailedToPending(maxRetries: Int = 5)
}
