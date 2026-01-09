package com.ikanisa.smsgateway

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.messaging.FirebaseMessaging
import com.ikanisa.smsgateway.notification.FcmTokenRegistrationService
import com.ikanisa.smsgateway.notification.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application class for SMS Gateway with notification support.
 */
@HiltAndroidApp
class SmsGatewayApplication : Application(), Configuration.Provider {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var notificationScheduler: NotificationScheduler
    
    @Inject
    lateinit var fcmTokenRegistrationService: FcmTokenRegistrationService
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize daily reminder scheduling (default: 9:00 AM)
        // This can be configured via settings later
        try {
            notificationScheduler.scheduleDailyReminders(hour = 9, minute = 0)
            Log.d("SmsGatewayApplication", "Daily reminders scheduled for 9:00 AM")
        } catch (e: Exception) {
            Log.e("SmsGatewayApplication", "Failed to schedule daily reminders", e)
        }
        
        // Initialize Firebase Cloud Messaging token registration
        initializeFcmToken()
    }
    
    private fun initializeFcmToken() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("SmsGatewayApplication", "Failed to get FCM token", task.exception)
                    return@addOnCompleteListener
                }
                
                val token = task.result
                Log.d("SmsGatewayApplication", "FCM token obtained: ${token.take(20)}...")
                
                // Register token with Supabase
                applicationScope.launch {
                    when (val result = fcmTokenRegistrationService.registerToken(token)) {
                        is com.ikanisa.smsgateway.data.Result.Success -> {
                            Log.d("SmsGatewayApplication", "FCM token registered with Supabase")
                        }
                        is com.ikanisa.smsgateway.data.Result.Error -> {
                            Log.e("SmsGatewayApplication", "Failed to register FCM token: ${result.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SmsGatewayApplication", "Error initializing FCM", e)
        }
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
}
