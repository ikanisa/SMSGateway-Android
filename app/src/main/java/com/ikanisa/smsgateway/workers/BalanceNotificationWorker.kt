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
 * Worker that sends balance update notifications to payers after payment allocation.
 * Triggered when a payment is allocated to a payer.
 */
@HiltWorker
class BalanceNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationRepository: NotificationRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get parameters from input data
            val payerId = inputData.getString("payerId")
            val paymentAmount = inputData.getString("paymentAmount") ?: "0"
            val currency = inputData.getString("currency") ?: "RWF"
            val transactionType = inputData.getString("transactionType") ?: "payment allocation"
            
            if (payerId == null) {
                android.util.Log.e("BalanceNotificationWorker", "Missing payerId in input data")
                return@withContext Result.failure()
            }
            
            // Get payer details
            val payerResult = notificationRepository.getPayerById(payerId)
            
            when (payerResult) {
                is com.ikanisa.smsgateway.data.Result.Success -> {
                    val payer = payerResult.data
                    
                    val parameters = mapOf(
                        "memberName" to (payer.name ?: "Member"),
                        "balance" to String.format("%.2f", payer.balance),
                        "currency" to payer.currency,
                        "paymentAmount" to paymentAmount,
                        "transactionType" to transactionType
                    )
                    
                    val message = NotificationMessageBuilder.buildMessage(
                        NotificationType.BALANCE_UPDATE,
                        parameters
                    )
                    
                    // Send notification
                    val result = notificationRepository.sendNotificationToPayer(
                        payerId = payer.id,
                        type = NotificationType.BALANCE_UPDATE.name,
                        message = message,
                        parameters = parameters
                    )
                    
                    when (result) {
                        is com.ikanisa.smsgateway.data.Result.Success -> {
                            android.util.Log.d("BalanceNotificationWorker", "Balance notification sent to ${payer.phoneNumber}")
                            Result.success()
                        }
                        is com.ikanisa.smsgateway.data.Result.Error -> {
                            android.util.Log.e("BalanceNotificationWorker", "Failed to send balance notification: ${result.message}")
                            Result.retry()
                        }
                    }
                }
                is com.ikanisa.smsgateway.data.Result.Error -> {
                    android.util.Log.e("BalanceNotificationWorker", "Failed to fetch payer: ${payerResult.message}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("BalanceNotificationWorker", "Error in balance notification worker", e)
            Result.retry()
        }
    }
}