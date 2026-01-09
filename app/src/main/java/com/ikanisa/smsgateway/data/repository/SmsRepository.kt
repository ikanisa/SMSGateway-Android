package com.ikanisa.smsgateway.data.repository

import com.ikanisa.smsgateway.data.Result
import com.ikanisa.smsgateway.data.model.SmsMessage
import com.ikanisa.smsgateway.data.model.SmsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Simple repository interface for SMS operations.
 */
interface SmsRepository {
    suspend fun sendSmsToBackend(sms: SmsMessage): Result<SmsResponse>
    fun observeSmsCount(): StateFlow<Int>
    fun observeErrorCount(): StateFlow<Int>
    suspend fun getSmsCount(): Int
    suspend fun getErrorCount(): Int
    suspend fun resetCounters()
}
