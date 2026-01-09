package com.ikanisa.smsgateway.data.datasource

import com.ikanisa.smsgateway.AppDefaults
import com.ikanisa.smsgateway.data.Result
import com.ikanisa.smsgateway.data.model.SmsMessage
import com.ikanisa.smsgateway.data.model.SmsResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Simplified Supabase API client.
 * Direct SMS forwarding to backend.
 */
class SupabaseApi {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()
    
    /**
     * Send SMS to Supabase ingest endpoint.
     */
    suspend fun ingestSms(sms: SmsMessage): Result<SmsResponse> {
        val url = "${AppDefaults.SUPABASE_URL.removeSuffix("/")}/functions/v1/ingest-sms"
        
        val momoCode = AppDefaults.MOMO_CODE
        if (momoCode.isEmpty()) {
            return Result.Error(
                message = "MOMO_CODE not configured",
                exception = IllegalStateException("MOMO_CODE must be set in BuildConfig")
            )
        }
        
        val payload = JSONObject().apply {
            put("momo_code", momoCode)
            put("sender", sms.sender)
            put("body", sms.body)
            put("received_at", iso8601UtcFromMillis(sms.timestampMillis))
            if (sms.simSlot != null) put("sim_slot", sms.simSlot)
        }
        
        val request = Request.Builder()
            .url(url)
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .addHeader("apikey", AppDefaults.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${AppDefaults.SUPABASE_ANON_KEY}")
            .build()
        
        return try {
            client.newCall(request).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                
                if (response.isSuccessful) {
                    try {
                        val json = JSONObject(responseText)
                        Result.Success(
                            SmsResponse(
                                id = json.optString("id").takeIf { it.isNotEmpty() },
                                parseStatus = json.optString("parse_status").takeIf { it.isNotEmpty() },
                                modelUsed = json.optString("model_used").takeIf { it.isNotEmpty() }
                            )
                        )
                    } catch (e: Exception) {
                        // Non-JSON response but still successful
                        Result.Success(
                            SmsResponse(
                                id = null,
                                parseStatus = "saved",
                                modelUsed = null
                            )
                        )
                    }
                } else {
                    val errorMessage = when {
                        responseText.isNotEmpty() -> {
                            try {
                                val errorJson = JSONObject(responseText)
                                errorJson.optString("error") ?: errorJson.optString("message") ?: "HTTP ${response.code}"
                            } catch (_: Exception) {
                                responseText.take(200)
                            }
                        }
                        else -> "HTTP ${response.code}"
                    }
                    
                    Result.Error(
                        message = errorMessage,
                        exception = when (response.code) {
                            in 400..499 -> IllegalArgumentException("Client error: $errorMessage")
                            in 500..599 -> IllegalStateException("Server error: $errorMessage")
                            else -> Exception("Network error: $errorMessage")
                        }
                    )
                }
            }
        } catch (e: Exception) {
            Result.Error(
                message = "Network exception: ${e.message ?: "Unknown error"}",
                exception = e
            )
        }
    }
    
    private fun iso8601UtcFromMillis(ms: Long): String {
        val instant = Instant.ofEpochMilli(ms)
        return DateTimeFormatter.ISO_INSTANT.format(instant)
    }
}
