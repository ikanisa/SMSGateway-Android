package com.ikanisa.smsgateway

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ikanisa.smsgateway.data.datasource.SupabaseApi
import com.ikanisa.smsgateway.data.model.SmsMessage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Simple worker that sends SMS directly to Supabase.
 */
@HiltWorker
class ProcessSmsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val api = SupabaseApi()

    override suspend fun doWork(): androidx.work.ListenableWorker.Result = withContext(Dispatchers.IO) {
        val sender = inputData.getString("sender") ?: "Unknown"
        val body = inputData.getString("messageBody") ?: ""
        val tsMillis = inputData.getLong("timestampMillis", System.currentTimeMillis())
        val simSlot = inputData.getInt("simSlot", -1).takeIf { it >= 0 }

        val sms = SmsMessage(
            sender = sender,
            body = body,
            timestampMillis = tsMillis,
            simSlot = simSlot
        )

        return@withContext when (val result = api.ingestSms(sms)) {
            is com.ikanisa.smsgateway.data.Result.Success -> {
                androidx.work.ListenableWorker.Result.success()
            }
            is com.ikanisa.smsgateway.data.Result.Error -> {
                // Retry on network/server errors, fail on client errors
                val shouldRetry = result.exception !is IllegalArgumentException
                if (shouldRetry) {
                    androidx.work.ListenableWorker.Result.retry()
                } else {
                    androidx.work.ListenableWorker.Result.failure()
                }
            }
        }
    }
}
