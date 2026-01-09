package com.example.smsgateway.data.datasource

import com.example.smsgateway.data.Result
import com.example.smsgateway.data.model.SmsMessage
import com.example.smsgateway.data.model.SmsResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Data source for Supabase API calls.
 * Handles all network communication with the backend.
 */
class SupabaseApi(
    private val supabaseUrl: String,
    private val supabaseAnonKey: String,
    private val deviceId: String,
    private val deviceSecret: String,
    private val deviceLabel: String? = null,
    private val momoMsisdn: String? = null,
    private val momoCode: String? = null
) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()
    
    /**
     * Send SMS to Supabase ingest endpoint.
     */
    suspend fun ingestSms(sms: SmsMessage): Result<SmsResponse> {
        val url = "${supabaseUrl.removeSuffix("/")}/functions/v1/ingest-sms"
        
        val payload = JSONObject().apply {
            put("device_id", deviceId)
            put("device_secret", deviceSecret)
            put("sender", sms.sender)
            put("body", sms.body)
            put("received_at", iso8601UtcFromMillis(sms.timestampMillis))
            if (sms.simSlot != null) put("sim_slot", sms.simSlot)
            if (deviceLabel != null && deviceLabel.isNotEmpty()) put("device_label", deviceLabel)
            
            val meta = JSONObject()
            if (momoMsisdn != null && momoMsisdn.isNotEmpty()) meta.put("momo_msisdn", momoMsisdn)
            if (momoCode != null && momoCode.isNotEmpty()) meta.put("momo_code", momoCode)
            if (meta.length() > 0) put("meta", meta)
        }
        
        val request = Request.Builder()
            .url(url)
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .addHeader("apikey", supabaseAnonKey)
            .addHeader("Authorization", "Bearer $supabaseAnonKey")
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
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(ms))
    }
}
