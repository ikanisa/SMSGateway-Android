package com.ikanisa.smsgateway.util

/**
 * Application-wide constants.
 */
object Constants {
    
    // Network timeouts
    const val NETWORK_CONNECT_TIMEOUT_SECONDS = 30L
    const val NETWORK_READ_TIMEOUT_SECONDS = 30L
    const val NETWORK_WRITE_TIMEOUT_SECONDS = 30L
    
    // WorkManager tags
    const val WORK_TAG_SMS_SYNC = "sms_sync"
    const val WORK_TAG_NOTIFICATION = "notification"
    const val WORK_TAG_DAILY_REMINDER = "daily_reminder"
    
    // SharedPreferences keys
    const val PREF_KEY_DEVICE_ACTIVATED = "device_activated"
    const val PREF_KEY_MOMO_CODE = "momo_code"
    const val PREF_KEY_FCM_TOKEN = "fcm_token"
    const val PREF_KEY_LAST_SYNC = "last_sync"
    
    // Intent actions
    const val ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
    
    // Notification channels
    const val NOTIFICATION_CHANNEL_SMS = "sms_channel"
    const val NOTIFICATION_CHANNEL_SYNC = "sync_channel"
    
    // Database
    const val DATABASE_NAME = "sms_gateway_db"
    const val DATABASE_VERSION = 1
    
    // Retry configuration
    const val MAX_RETRY_ATTEMPTS = 3
    const val INITIAL_BACKOFF_MILLIS = 1000L
}
