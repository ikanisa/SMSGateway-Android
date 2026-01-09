package com.ikanisa.smsgateway.data.model

/**
 * Represents an SMS message received by the device.
 */
data class SmsMessage(
    val sender: String,
    val body: String,
    val timestampMillis: Long,
    val simSlot: Int? = null
)
