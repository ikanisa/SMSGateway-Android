package com.ikanisa.smsgateway.notification

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.ikanisa.smsgateway.data.Result
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for sending SMS notifications directly from the device.
 */
@Singleton
class SmsNotificationService @Inject constructor(
    private val context: Context
) {
    private val smsManager: SmsManager by lazy {
        SmsManager.getDefault()
    }
    
    private val TAG = "SmsNotificationService"
    
    /**
     * Send SMS notification to a single phone number.
     */
    suspend fun sendSms(phoneNumber: String, message: String): Result<Unit> {
        return try {
            if (phoneNumber.isBlank() || message.isBlank()) {
                return Result.Error("Phone number and message cannot be empty")
            }
            
            // Split message if it's too long (SMS limit is 160 chars per part)
            val parts = smsManager.divideMessage(message)
            
            if (parts.size == 1) {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            } else {
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            }
            
            Log.d(TAG, "SMS sent successfully to $phoneNumber")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $phoneNumber", e)
            Result.Error(e.message ?: "Unknown error", e)
        }
    }
    
    /**
     * Send SMS notifications to multiple phone numbers.
     */
    suspend fun sendBulkSms(phoneNumbers: List<String>, message: String): Map<String, Result<Unit>> {
        return phoneNumbers.associateWith { phoneNumber ->
            sendSms(phoneNumber, message)
        }
    }
}