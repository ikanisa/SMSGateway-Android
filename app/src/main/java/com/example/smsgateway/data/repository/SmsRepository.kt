package com.example.smsgateway.data.repository

import com.example.smsgateway.data.Result
import com.example.smsgateway.data.model.SmsMessage
import com.example.smsgateway.data.model.SmsResponse
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for SMS operations.
 * Provides a clean abstraction for data operations.
 */
interface SmsRepository {
    /**
     * Send an SMS message to the Supabase backend.
     * @param sms The SMS message to send
     * @return Result containing the response or error
     */
    suspend fun sendSmsToBackend(sms: SmsMessage): Result<SmsResponse>
    
    /**
     * Observe the count of successfully processed SMS messages.
     */
    fun observeSmsCount(): Flow<Int>
    
    /**
     * Observe the count of errors encountered.
     */
    fun observeErrorCount(): Flow<Int>
    
    /**
     * Get the current SMS count.
     */
    suspend fun getSmsCount(): Int
    
    /**
     * Get the current error count.
     */
    suspend fun getErrorCount(): Int
    
    /**
     * Reset counters (for testing or manual reset).
     */
    suspend fun resetCounters()
}
