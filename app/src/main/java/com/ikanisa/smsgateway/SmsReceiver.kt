package com.ikanisa.smsgateway

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Simple SMS receiver that enqueues work for processing.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") return

        val bundle = intent.extras ?: return
        @Suppress("DEPRECATION")
        val pdus = bundle.get("pdus") as? Array<*> ?: return
        val format = bundle.getString("format")

        val messages = pdus.mapNotNull { p ->
            val bytes = p as? ByteArray ?: return@mapNotNull null
            SmsMessage.createFromPdu(bytes, format)
        }
        if (messages.isEmpty()) return

        // Merge multipart segments into ONE body
        val sender = messages.first().displayOriginatingAddress ?: "Unknown"
        val mergedBody = messages.joinToString(separator = "") { it.messageBody ?: "" }.trim()
        val tsMillis = messages.first().timestampMillis

        // Filter 1: Check sender is in telco allowlist
        if (!AppDefaults.isAllowedSender(sender)) {
            // Unknown sender - ignore (don't forward)
            return
        }

        // Filter 2: Check body matches MTN MoMo patterns
        if (!AppDefaults.matchesMomoPattern(mergedBody)) {
            // Body doesn't match known MoMo patterns - ignore
            return
        }

        // Best-effort SIM slot
        val simSlot = when {
            bundle.containsKey("simSlot") -> bundle.getInt("simSlot", -1)
            bundle.containsKey("slot") -> bundle.getInt("slot", -1)
            bundle.containsKey("slot_id") -> bundle.getInt("slot_id", -1)
            bundle.containsKey("phone") -> bundle.getInt("phone", -1)
            else -> -1
        }.takeIf { it >= 0 }

        // Passed filters - enqueue worker for processing
        val inputData = Data.Builder()
            .putString("sender", sender)
            .putString("messageBody", mergedBody)
            .putLong("timestampMillis", tsMillis)
            .apply { if (simSlot != null) putInt("simSlot", simSlot) }
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ProcessSmsWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
