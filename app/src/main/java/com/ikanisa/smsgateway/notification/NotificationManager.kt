package com.ikanisa.smsgateway.notification

import com.ikanisa.smsgateway.data.model.NotificationType
import com.ikanisa.smsgateway.data.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-level notification manager that coordinates notification sending.
 * Provides convenient methods for common notification scenarios.
 */
@Singleton
class NotificationManager @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val notificationScheduler: NotificationScheduler
) {
    
    /**
     * Send daily reminder to all active payers.
     */
    suspend fun sendDailyRemindersToAll() {
        val message = NotificationMessageBuilder.buildMessage(
            NotificationType.DAILY_REMINDER,
            mapOf("groupName" to "Burimunsi Production")
        )
        
        notificationRepository.sendNotificationToAll(
            type = NotificationType.DAILY_REMINDER.name,
            message = message
        )
    }
    
    /**
     * Send balance update notification to a specific payer.
     */
    suspend fun sendBalanceUpdateToPayer(
        payerId: String,
        balance: Double,
        currency: String = "RWF",
        paymentAmount: Double? = null
    ) {
        val parameters = buildMap {
            put("balance", String.format("%.2f", balance))
            put("currency", currency)
            paymentAmount?.let { put("paymentAmount", String.format("%.2f", it)) }
        }
        
        val message = NotificationMessageBuilder.buildMessage(
            NotificationType.BALANCE_UPDATE,
            parameters
        )
        
        notificationRepository.sendNotificationToPayer(
            payerId = payerId,
            type = NotificationType.BALANCE_UPDATE.name,
            message = message,
            parameters = parameters
        )
    }
    
    /**
     * Trigger balance notification worker when payment is allocated.
     * This is typically called after a payment allocation event.
     */
    fun notifyBalanceAfterPayment(
        payerId: String,
        paymentAmount: Double,
        currency: String = "RWF"
    ) {
        notificationScheduler.sendBalanceNotification(
            payerId = payerId,
            paymentAmount = String.format("%.2f", paymentAmount),
            currency = currency,
            transactionType = "payment allocation"
        )
    }
    
    /**
     * Send burimunsi production notification to all payers.
     */
    suspend fun sendBurimunsiProductionNotification(
        message: String,
        details: String? = null
    ) {
        val parameters = buildMap {
            put("message", message)
            details?.let { put("details", it) }
        }
        
        val fullMessage = NotificationMessageBuilder.buildMessage(
            NotificationType.BURIMUNSI_PRODUCTION,
            parameters
        )
        
        notificationRepository.sendNotificationToAll(
            type = NotificationType.BURIMUNSI_PRODUCTION.name,
            message = fullMessage,
            parameters = parameters
        )
    }
    
    /**
     * Send notification to specific phone numbers.
     */
    suspend fun sendNotificationToPhoneNumbers(
        phoneNumbers: List<String>,
        type: NotificationType,
        message: String,
        parameters: Map<String, String> = emptyMap()
    ) {
        val request = com.ikanisa.smsgateway.data.model.NotificationRequest(
            type = type.name,
            recipientPhoneNumbers = phoneNumbers,
            message = message,
            parameters = parameters
        )
        
        notificationRepository.sendNotification(request)
    }
    
    /**
     * Configure daily reminder schedule.
     */
    fun configureDailyReminders(hour: Int, minute: Int) {
        notificationScheduler.scheduleDailyReminders(hour, minute)
    }
    
    /**
     * Cancel daily reminder schedule.
     */
    fun cancelDailyReminders() {
        notificationScheduler.cancelDailyReminders()
    }
}