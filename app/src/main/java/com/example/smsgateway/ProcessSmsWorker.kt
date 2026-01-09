package com.example.smsgateway

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smsgateway.data.model.SmsMessage
import com.example.smsgateway.data.repository.SmsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * WorkManager worker that processes incoming SMS messages.
 * Uses Repository pattern with Hilt dependency injection.
 */
@HiltWorker
class ProcessSmsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: SmsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): androidx.work.ListenableWorker.Result = withContext(Dispatchers.IO) {
        val sender = inputData.getString("sender") ?: "Unknown"
        val body = inputData.getString("messageBody") ?: ""
        val tsMillis = inputData.getLong("timestampMillis", System.currentTimeMillis())
        val simSlot = inputData.getInt("simSlot", -1).takeIf { it >= 0 }

        // Create SmsMessage from input data
        val sms = SmsMessage(
            sender = sender,
            body = body,
            timestampMillis = tsMillis,
            simSlot = simSlot
        )

        // Use repository to send SMS
        return@withContext when (val result = repository.sendSmsToBackend(sms)) {
            is com.example.smsgateway.data.Result.Success -> {
                // Success - WorkManager will mark as success
                androidx.work.ListenableWorker.Result.success()
            }
            is com.example.smsgateway.data.Result.Error -> {
                // Determine retry strategy based on error type
                val shouldRetry = when (result.exception) {
                    is IllegalArgumentException -> false // Client errors (4xx) - don't retry
                    is IllegalStateException -> true // Server errors (5xx) - retry
                    else -> true // Network errors - retry
                }
                
                if (shouldRetry) {
                    androidx.work.ListenableWorker.Result.retry()
                } else {
                    androidx.work.ListenableWorker.Result.failure()
                }
            }
        }
    }
}
