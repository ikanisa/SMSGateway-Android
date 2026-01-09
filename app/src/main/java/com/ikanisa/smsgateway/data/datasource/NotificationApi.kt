package com.ikanisa.smsgateway.data.datasource

import com.ikanisa.smsgateway.AppDefaults
import com.ikanisa.smsgateway.data.Result
import com.ikanisa.smsgateway.data.model.NotificationRequest
import com.ikanisa.smsgateway.data.model.NotificationResponse
import com.ikanisa.smsgateway.data.model.Payer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * API client for notification operations with Supabase.
 */
class NotificationApi {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()
    
    /**
     * Send notification via Supabase edge function.
     */
    suspend fun sendNotification(request: NotificationRequest): Result<NotificationResponse> {
        val url = "${AppDefaults.SUPABASE_URL.removeSuffix("/")}/functions/v1/send-notification"
        
        val payload = JSONObject().apply {
            put("type", request.type)
            put("message", request.message)
            put("sendToAll", request.sendToAll)
            if (request.recipientPhoneNumbers.isNotEmpty()) {
                put("recipientPhoneNumbers", JSONArray(request.recipientPhoneNumbers))
            }
            if (request.payerIds.isNotEmpty()) {
                put("payerIds", JSONArray(request.payerIds))
            }
            if (request.parameters.isNotEmpty()) {
                put("parameters", JSONObject(request.parameters))
            }
            request.scheduledFor?.let { put("scheduledFor", it) }
        }
        
        val httpRequest = Request.Builder()
            .url(url)
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .addHeader("apikey", AppDefaults.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${AppDefaults.SUPABASE_ANON_KEY}")
            .build()
        
        return try {
            client.newCall(httpRequest).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                
                if (response.isSuccessful) {
                    try {
                        val json = JSONObject(responseText)
                        Result.Success(
                            NotificationResponse(
                                success = json.optBoolean("success", true),
                                message = json.optString("message", "Notification sent"),
                                sentCount = json.optInt("sentCount", 0),
                                failedCount = json.optInt("failedCount", 0),
                                notificationIds = parseStringArray(json.optJSONArray("notificationIds")),
                                errors = parseStringArray(json.optJSONArray("errors"))
                            )
                        )
                    } catch (e: Exception) {
                        Result.Success(
                            NotificationResponse(
                                success = true,
                                message = "Notification sent",
                                sentCount = 1
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
    
    /**
     * Fetch all payers from Supabase.
     */
    suspend fun getAllPayers(): Result<List<Payer>> {
        val url = "${AppDefaults.SUPABASE_URL.removeSuffix("/")}/rest/v1/payers?select=*&is_active=eq.true"
        
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("apikey", AppDefaults.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${AppDefaults.SUPABASE_ANON_KEY}")
            .addHeader("Content-Type", "application/json")
            .build()
        
        return try {
            client.newCall(request).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                
                if (response.isSuccessful) {
                    try {
                        val jsonArray = JSONArray(responseText)
                        val payers = mutableListOf<Payer>()
                        for (i in 0 until jsonArray.length()) {
                            val payerJson = jsonArray.getJSONObject(i)
                            payers.add(
                                Payer(
                                    id = payerJson.getString("id"),
                                    phoneNumber = payerJson.getString("phone_number"),
                                    name = payerJson.optString("name", null),
                                    balance = payerJson.optDouble("balance", 0.0),
                                    currency = payerJson.optString("currency", "RWF"),
                                    isActive = payerJson.optBoolean("is_active", true),
                                    groupId = payerJson.optString("group_id", null),
                                    createdAt = payerJson.optString("created_at", null),
                                    updatedAt = payerJson.optString("updated_at", null)
                                )
                            )
                        }
                        Result.Success(payers)
                    } catch (e: Exception) {
                        Result.Error(
                            message = "Failed to parse payers: ${e.message}",
                            exception = e
                        )
                    }
                } else {
                    Result.Error(
                        message = "HTTP ${response.code}: $responseText",
                        exception = Exception("Failed to fetch payers")
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
    
    /**
     * Fetch payer by ID.
     */
    suspend fun getPayerById(payerId: String): Result<Payer> {
        val url = "${AppDefaults.SUPABASE_URL.removeSuffix("/")}/rest/v1/payers?id=eq.$payerId&select=*&limit=1"
        
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("apikey", AppDefaults.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${AppDefaults.SUPABASE_ANON_KEY}")
            .addHeader("Content-Type", "application/json")
            .build()
        
        return try {
            client.newCall(request).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                
                if (response.isSuccessful) {
                    try {
                        val jsonArray = JSONArray(responseText)
                        if (jsonArray.length() > 0) {
                            val payerJson = jsonArray.getJSONObject(0)
                            Result.Success(
                                Payer(
                                    id = payerJson.getString("id"),
                                    phoneNumber = payerJson.getString("phone_number"),
                                    name = payerJson.optString("name", null),
                                    balance = payerJson.optDouble("balance", 0.0),
                                    currency = payerJson.optString("currency", "RWF"),
                                    isActive = payerJson.optBoolean("is_active", true),
                                    groupId = payerJson.optString("group_id", null),
                                    createdAt = payerJson.optString("created_at", null),
                                    updatedAt = payerJson.optString("updated_at", null)
                                )
                            )
                        } else {
                            Result.Error(
                                message = "Payer not found",
                                exception = NoSuchElementException("Payer with ID $payerId not found")
                            )
                        }
                    } catch (e: Exception) {
                        Result.Error(
                            message = "Failed to parse payer: ${e.message}",
                            exception = e
                        )
                    }
                } else {
                    Result.Error(
                        message = "HTTP ${response.code}: $responseText",
                        exception = Exception("Failed to fetch payer")
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
    
    private fun parseStringArray(jsonArray: JSONArray?): List<String> {
        if (jsonArray == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        return list
    }
}