package com.ikanisa.smsgateway.data.model

import kotlinx.serialization.Serializable

/**
 * Request model for sending notifications.
 */
@Serializable
data class NotificationRequest(
    /**
     * Type of notification to send.
     */
    val type: String,
    
    /**
     * Phone number(s) to send notification to.
     * If empty, sends to all payers.
     */
    val recipientPhoneNumbers: List<String> = emptyList(),
    
    /**
     * Message content for the notification.
     */
    val message: String,
    
    /**
     * Optional parameters for dynamic message content.
     */
    val parameters: Map<String, String> = emptyMap(),
    
    /**
     * Optional: Specific payer ID(s) to notify.
     */
    val payerIds: List<String> = emptyList(),
    
    /**
     * Whether to send to all payers if recipientPhoneNumbers is empty.
     */
    val sendToAll: Boolean = false,
    
    /**
     * Optional: Schedule the notification for later (ISO 8601 timestamp).
     */
    val scheduledFor: String? = null
)

/**
 * Response model for notification sending.
 */
@Serializable
data class NotificationResponse(
    val success: Boolean,
    val message: String,
    val sentCount: Int = 0,
    val failedCount: Int = 0,
    val notificationIds: List<String> = emptyList(),
    val errors: List<String> = emptyList()
)