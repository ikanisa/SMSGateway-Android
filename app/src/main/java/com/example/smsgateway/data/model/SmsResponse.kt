package com.example.smsgateway.data.model

/**
 * Response from Supabase after ingesting an SMS.
 */
data class SmsResponse(
    val id: String?,
    val parseStatus: String?,
    val modelUsed: String?,
    val error: String? = null
)
