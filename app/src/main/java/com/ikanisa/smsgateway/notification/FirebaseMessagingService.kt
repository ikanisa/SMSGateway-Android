package com.ikanisa.smsgateway.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Firebase Cloud Messaging service for receiving push notifications.
 * This can be used to trigger SMS notifications remotely.
 */
@AndroidEntryPoint
class AppFirebaseMessagingService : FirebaseMessagingService() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    @Inject
    lateinit var fcmTokenRegistrationService: FcmTokenRegistrationService
    
    @Inject
    lateinit var notificationScheduler: NotificationScheduler
    
    private val TAG = "FirebaseMessagingService"
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        
        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleNotificationData(remoteMessage.data)
        }
        
        // Check if message contains notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message notification body: ${it.body}")
            // Handle notification payload if needed
            // For SMS gateway, we primarily use data messages to trigger actions
        }
    }
    
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed FCM token: $token")
        // Send token to Supabase to enable push notifications
        serviceScope.launch {
            when (val result = fcmTokenRegistrationService.registerToken(token)) {
                is com.ikanisa.smsgateway.data.Result.Success -> {
                    Log.d(TAG, "FCM token registered successfully with Supabase")
                }
                is com.ikanisa.smsgateway.data.Result.Error -> {
                    Log.e(TAG, "Failed to register FCM token: ${result.message}")
                }
            }
        }
    }
    
    private fun handleNotificationData(data: Map<String, String>) {
        val action = data["action"]
        
        when (action) {
            "send_daily_reminders" -> {
                Log.d(TAG, "Triggering daily reminders via FCM")
                // Trigger daily reminder worker
                serviceScope.launch {
                    // The daily reminder is already scheduled, but we can trigger it immediately
                    // via NotificationManager if needed
                    Log.d(TAG, "Daily reminder triggered via FCM push notification")
                }
            }
            "send_balance_notification" -> {
                val payerId = data["payerId"]
                val paymentAmount = data["paymentAmount"] ?: "0"
                val currency = data["currency"] ?: "RWF"
                val transactionType = data["transactionType"] ?: "payment allocation"
                
                if (payerId != null) {
                    Log.d(TAG, "Triggering balance notification for payer: $payerId")
                    notificationScheduler.sendBalanceNotification(
                        payerId = payerId,
                        paymentAmount = paymentAmount,
                        currency = currency,
                        transactionType = transactionType
                    )
                } else {
                    Log.w(TAG, "Balance notification triggered but payerId is missing")
                }
            }
            "send_custom_notification" -> {
                val type = data["notificationType"] ?: "GENERAL"
                val message = data["message"] ?: ""
                val payerIds = data["payerIds"]?.split(",")
                
                Log.d(TAG, "Custom notification triggered: type=$type, message=$message")
                // Handle custom notification - could be integrated with NotificationManager
            }
            else -> {
                Log.d(TAG, "Unknown action: $action, data: $data")
            }
        }
    }
}