package com.ikanisa.smsgateway.data.repository

import com.ikanisa.smsgateway.data.Result
import com.ikanisa.smsgateway.data.datasource.NotificationApi
import com.ikanisa.smsgateway.data.model.NotificationRequest
import com.ikanisa.smsgateway.data.model.NotificationResponse
import com.ikanisa.smsgateway.data.model.Payer
import com.ikanisa.smsgateway.notification.SmsNotificationService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of NotificationRepository.
 */
@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi,
    private val smsNotificationService: SmsNotificationService
) : NotificationRepository {
    
    override suspend fun sendNotification(request: NotificationRequest): Result<NotificationResponse> {
        return notificationApi.sendNotification(request)
    }
    
    override suspend fun sendNotificationToPayer(
        payerId: String,
        type: String,
        message: String,
        parameters: Map<String, String>
    ): Result<NotificationResponse> {
        // First, get the payer to retrieve phone number
        return when (val payerResult = notificationApi.getPayerById(payerId)) {
            is Result.Success -> {
                val payer = payerResult.data
                val request = NotificationRequest(
                    type = type,
                    recipientPhoneNumbers = listOf(payer.phoneNumber),
                    message = message,
                    parameters = parameters + mapOf("memberName" to (payer.name ?: "Member")),
                    payerIds = listOf(payerId)
                )
                notificationApi.sendNotification(request)
            }
            is Result.Error -> payerResult
        }
    }
    
    override suspend fun sendNotificationToAll(
        type: String,
        message: String,
        parameters: Map<String, String>
    ): Result<NotificationResponse> {
        val request = NotificationRequest(
            type = type,
            message = message,
            parameters = parameters,
            sendToAll = true
        )
        return notificationApi.sendNotification(request)
    }
    
    override suspend fun getAllPayers(): Result<List<Payer>> {
        return notificationApi.getAllPayers()
    }
    
    override suspend fun getPayerById(payerId: String): Result<Payer> {
        return notificationApi.getPayerById(payerId)
    }
    
    override suspend fun sendLocalSms(phoneNumber: String, message: String): Result<Unit> {
        return smsNotificationService.sendSms(phoneNumber, message)
    }
    
    override suspend fun sendLocalBulkSms(phoneNumbers: List<String>, message: String): Map<String, Result<Unit>> {
        return smsNotificationService.sendBulkSms(phoneNumbers, message)
    }
}