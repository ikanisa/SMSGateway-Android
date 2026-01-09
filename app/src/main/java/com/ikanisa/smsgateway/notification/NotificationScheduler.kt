package com.ikanisa.smsgateway.notification

import androidx.work.*
import com.ikanisa.smsgateway.workers.BalanceNotificationWorker
import com.ikanisa.smsgateway.workers.DailyReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for scheduling notification workers.
 */
@Singleton
class NotificationScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    
    companion object {
        private const val DAILY_REMINDER_WORK_NAME = "daily_reminder_work"
        private const val DAILY_REMINDER_TAG = "daily_reminder"
        
        // Default time for daily reminders: 9:00 AM
        private const val DEFAULT_HOUR = 9
        private const val DEFAULT_MINUTE = 0
    }
    
    /**
     * Schedule daily reminder notifications at the specified time.
     * If already scheduled, updates the schedule.
     * 
     * @param hour Hour of day (0-23)
     * @param minute Minute of hour (0-59)
     */
    fun scheduleDailyReminders(hour: Int = DEFAULT_HOUR, minute: Int = DEFAULT_MINUTE) {
        // Cancel existing daily reminder work
        workManager.cancelUniqueWork(DAILY_REMINDER_WORK_NAME)
        
        // Calculate initial delay until the specified time
        val calendar = Calendar.getInstance()
        val now = calendar.clone() as Calendar
        
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // If the time has passed today, schedule for tomorrow
        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val initialDelay = calendar.timeInMillis - now.timeInMillis
        
        // Create periodic work request (daily)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val dailyReminderWork = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(DAILY_REMINDER_TAG)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyReminderWork
        )
    }
    
    /**
     * Cancel daily reminder notifications.
     */
    fun cancelDailyReminders() {
        workManager.cancelUniqueWork(DAILY_REMINDER_WORK_NAME)
    }
    
    /**
     * Send balance notification immediately when payment is allocated.
     */
    fun sendBalanceNotification(
        payerId: String,
        paymentAmount: String,
        currency: String = "RWF",
        transactionType: String = "payment allocation"
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val inputData = Data.Builder()
            .putString("payerId", payerId)
            .putString("paymentAmount", paymentAmount)
            .putString("currency", currency)
            .putString("transactionType", transactionType)
            .build()
        
        val balanceNotificationWork = OneTimeWorkRequestBuilder<BalanceNotificationWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag("balance_notification")
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueue(balanceNotificationWork)
    }
}