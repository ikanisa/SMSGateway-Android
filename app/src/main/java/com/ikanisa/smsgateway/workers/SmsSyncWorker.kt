package com.ikanisa.smsgateway.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ikanisa.smsgateway.data.Result
import com.ikanisa.smsgateway.data.repository.SmsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Periodic worker that syncs pending SMS messages to the backend.
 * 
 * Runs every 15 minutes when:
 * - Network is connected
 * - Battery is not low
 * 
 * This ensures offline-queued messages are eventually synced when
 * connectivity is restored.
 */
@HiltWorker
class SmsSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val smsRepository: SmsRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "sms_sync_work"
        const val TAG = "sms_sync"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Timber.d("SmsSyncWorker started, attempt: $runAttemptCount")
        
        try {
            when (val result = smsRepository.syncPendingMessages()) {
                is com.ikanisa.smsgateway.data.Result.Success -> {
                    val syncedCount = result.data
                    Timber.i("SmsSyncWorker completed: synced $syncedCount messages")
                    
                    // Always succeed even if 0 messages - work is complete
                    Result.success()
                }
                is com.ikanisa.smsgateway.data.Result.Error -> {
                    Timber.w("SmsSyncWorker failed: ${result.message}")
                    
                    // Retry on transient errors, but not indefinitely
                    if (runAttemptCount < 3) {
                        Timber.d("Will retry sync (attempt ${runAttemptCount + 1}/3)")
                        Result.retry()
                    } else {
                        Timber.e("SmsSyncWorker exhausted retries, marking as failure")
                        Result.failure()
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "SmsSyncWorker exception")
            
            // Retry on unexpected exceptions
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
