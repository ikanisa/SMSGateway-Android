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
import timber.log.Timber
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
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.i("SMS Gateway Application started")
        
        // Initialize daily reminder scheduling (default: 9:00 AM)
        // This can be configured via settings later
        try {
            notificationScheduler.scheduleDailyReminders(hour = 9, minute = 0)
            Timber.d("Daily reminders scheduled for 9:00 AM")
        } catch (e: Exception) {
            Timber.e(e, "Failed to schedule daily reminders")
        }
        
        // Initialize periodic SMS sync to flush offline queue
        try {
            notificationScheduler.scheduleSmsSyncWork()
            Timber.d("SMS sync worker scheduled (every 15 minutes)")
        } catch (e: Exception) {
            Timber.e(e, "Failed to schedule SMS sync worker")
        }
        
        // Initialize Firebase Cloud Messaging token registration
        initializeFcmToken()
    }
    
    private fun initializeFcmToken() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.w(task.exception, "Failed to get FCM token")
                    return@addOnCompleteListener
                }
                
                val token = task.result
                Timber.d("FCM token obtained: ${token.take(20)}...")
                
                // Register token with Supabase
                applicationScope.launch {
                    when (val result = fcmTokenRegistrationService.registerToken(token)) {
                        is com.ikanisa.smsgateway.data.Result.Success -> {
                            Timber.d("FCM token registered with Supabase")
                        }
                        is com.ikanisa.smsgateway.data.Result.Error -> {
                            Timber.e("Failed to register FCM token: ${result.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error initializing FCM")
        }
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
