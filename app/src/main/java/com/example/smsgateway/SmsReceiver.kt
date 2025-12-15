package com.example.smsgateway

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

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

        // Use SMS timestamp when available
        val tsMillis = messages.first().timestampMillis

        // Best-effort SIM slot (varies by device/vendor)
        val simSlot = when {
            bundle.containsKey("simSlot") -> bundle.getInt("simSlot", -1)
            bundle.containsKey("slot") -> bundle.getInt("slot", -1)
            bundle.containsKey("slot_id") -> bundle.getInt("slot_id", -1)
            bundle.containsKey("phone") -> bundle.getInt("phone", -1)
            else -> -1
        }

        val inputDataBuilder = Data.Builder()
            .putString("sender", sender)
            .putString("messageBody", mergedBody)
            .putLong("timestampMillis", tsMillis)

        if (simSlot >= 0) inputDataBuilder.putInt("simSlot", simSlot)

        val workRequest = OneTimeWorkRequestBuilder<ProcessSmsWorker>()
            .setInputData(inputDataBuilder.build())
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
