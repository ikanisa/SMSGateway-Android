package com.ikanisa.smsgateway.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ikanisa.smsgateway.data.model.NotificationType
import com.ikanisa.smsgateway.data.repository.NotificationRepository
import com.ikanisa.smsgateway.notification.NotificationMessageBuilder
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker that sends daily reminders to all active payers to continue contributions.
 * Scheduled to run daily (configure via WorkManager).
 */
@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationRepository: NotificationRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get all active payers
            val payersResult = notificationRepository.getAllPayers()
            
            when (payersResult) {
                is com.ikanisa.smsgateway.data.Result.Success -> {
                    val payers = payersResult.data
                    
                    if (payers.isEmpty()) {
                        android.util.Log.d("DailyReminderWorker", "No active payers found")
                        return@withContext Result.success()
                    }
                    
                    // Send daily reminder to each payer
                    var successCount = 0
                    var failureCount = 0
                    
                    payers.forEach { payer ->
                        val parameters = mapOf(
                            "memberName" to (payer.name ?: "Member"),
                            "groupName" to "Burimunsi Production",
                            "currency" to payer.currency
                        )
                        
                        val message = NotificationMessageBuilder.buildMessage(
                            NotificationType.DAILY_REMINDER,
                            parameters
                        )
                        
                        // Send via Supabase (which will handle SMS sending via backend)
                        val result = notificationRepository.sendNotificationToPayer(
                            payerId = payer.id,
                            type = NotificationType.DAILY_REMINDER.name,
                            message = message,
                            parameters = parameters
                        )
                        
                        when (result) {
                            is com.ikanisa.smsgateway.data.Result.Success -> {
                                successCount++
                                android.util.Log.d("DailyReminderWorker", "Reminder sent to ${payer.phoneNumber}")
                            }
                            is com.ikanisa.smsgateway.data.Result.Error -> {
                                failureCount++
                                android.util.Log.e("DailyReminderWorker", "Failed to send reminder to ${payer.phoneNumber}: ${result.message}")
                            }
                        }
                    }
                    
                    android.util.Log.d("DailyReminderWorker", "Daily reminders completed: $successCount succeeded, $failureCount failed")
                    
                    // Return success even if some failed - they will be retried
                    if (successCount > 0) {
                        Result.success()
                    } else {
                        Result.retry()
                    }
                }
                is com.ikanisa.smsgateway.data.Result.Error -> {
                    android.util.Log.e("DailyReminderWorker", "Failed to fetch payers: ${payersResult.message}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DailyReminderWorker", "Error in daily reminder worker", e)
            Result.retry()
        }
    }
}