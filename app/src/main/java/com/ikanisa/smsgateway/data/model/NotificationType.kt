package com.ikanisa.smsgateway.data.model

/**
 * Enumeration of notification types supported by the system.
 */
enum class NotificationType {
    /**
     * Daily reminder for members to continue contributions (burimunsi production).
     */
    DAILY_REMINDER,
    
    /**
     * Balance notification after payment allocation.
     */
    BALANCE_UPDATE,
    
    /**
     * Burimunsi production notification.
     */
    BURIMUNSI_PRODUCTION,
    
    /**
     * Payment received notification.
     */
    PAYMENT_RECEIVED,
    
    /**
     * Payment allocated notification.
     */
    PAYMENT_ALLOCATED,
    
    /**
     * General notification.
     */
    GENERAL
}