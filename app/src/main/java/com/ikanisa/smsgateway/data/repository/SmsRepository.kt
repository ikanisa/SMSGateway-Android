package com.ikanisa.smsgateway.data.repository

import com.ikanisa.smsgateway.data.Result
import com.ikanisa.smsgateway.data.model.SmsMessage
import com.ikanisa.smsgateway.data.model.SmsResponse
import com.ikanisa.smsgateway.domain.model.SyncStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for SMS operations with offline-first support.
 * 
 * Provides local storage, sync management, and backend communication.
 */
interface SmsRepository {
    
    // =========================================================================
    // Offline-First Operations
    // =========================================================================
    
    /**
     * Save SMS to local database with deduplication.
     * Attempts immediate sync if online.
     * 
     * @return Result containing the message ID or error
     */
    suspend fun saveSms(sms: SmsMessage): Result<String>
    
    /**
     * Observe all messages from local storage.
     */
    fun getAllMessages(): Flow<List<SmsMessage>>
    
    /**
     * Sync pending messages to backend.
     * 
     * @return Result containing count of synced messages
     */
    suspend fun syncPendingMessages(): Result<Int>
    
    /**
     * Observe sync statistics.
     */
    fun getSyncStats(): Flow<SyncStats>
    
    // =========================================================================
    // Legacy Operations (for backward compatibility)
    // =========================================================================
    
    /**
     * Send SMS directly to backend (bypasses local storage).
     * @deprecated Use saveSms() for offline-first behavior
     */
    suspend fun sendSmsToBackend(sms: SmsMessage): Result<SmsResponse>
    
    /**
     * Observe count of successfully processed SMS messages.
     */
    fun observeSmsCount(): StateFlow<Int>
    
    /**
     * Observe count of errors.
     */
    fun observeErrorCount(): StateFlow<Int>
    
    /**
     * Get current SMS count.
     */
    suspend fun getSmsCount(): Int
    
    /**
     * Get current error count.
     */
    suspend fun getErrorCount(): Int
    
    /**
     * Reset counters to zero.
     */
    suspend fun resetCounters()
}
