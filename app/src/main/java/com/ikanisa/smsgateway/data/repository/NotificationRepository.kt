package com.ikanisa.smsgateway.data.repository

import com.ikanisa.smsgateway.data.Result
import com.ikanisa.smsgateway.data.model.NotificationRequest
import com.ikanisa.smsgateway.data.model.NotificationResponse
import com.ikanisa.smsgateway.data.model.Payer
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for notification operations.
 */
interface NotificationRepository {
    /**
     * Send notification to specified recipients or all payers.
     */
    suspend fun sendNotification(request: NotificationRequest): Result<NotificationResponse>
    
    /**
     * Send notification to a single payer.
     */
    suspend fun sendNotificationToPayer(payerId: String, type: String, message: String, parameters: Map<String, String> = emptyMap()): Result<NotificationResponse>
    
    /**
     * Send notification to all payers.
     */
    suspend fun sendNotificationToAll(type: String, message: String, parameters: Map<String, String> = emptyMap()): Result<NotificationResponse>
    
    /**
     * Get all active payers.
     */
    suspend fun getAllPayers(): Result<List<Payer>>
    
    /**
     * Get payer by ID.
     */
    suspend fun getPayerById(payerId: String): Result<Payer>
    
    /**
     * Send SMS notification directly from device (local SMS sending).
     */
    suspend fun sendLocalSms(phoneNumber: String, message: String): Result<Unit>
    
    /**
     * Send bulk SMS notifications directly from device.
     */
    suspend fun sendLocalBulkSms(phoneNumbers: List<String>, message: String): Map<String, Result<Unit>>
}