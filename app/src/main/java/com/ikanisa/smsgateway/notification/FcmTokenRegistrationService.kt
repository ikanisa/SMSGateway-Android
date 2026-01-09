package com.ikanisa.smsgateway.notification

import android.util.Log
import com.ikanisa.smsgateway.AppDefaults
import com.ikanisa.smsgateway.data.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for registering FCM tokens with Supabase backend.
 */
@Singleton
class FcmTokenRegistrationService @Inject constructor() {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()
    
    private val TAG = "FcmTokenRegistration"
    
    /**
     * Register FCM token with Supabase.
     * This stores the token so the backend can send push notifications to this device.
     */
    suspend fun registerToken(
        fcmToken: String,
        deviceId: String? = null,
        userId: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Option 1: Store in Supabase table (requires fcm_tokens table)
            val url = "${AppDefaults.SUPABASE_URL.removeSuffix("/")}/rest/v1/fcm_tokens"
            
            val payload = JSONObject().apply {
                put("fcm_token", fcmToken)
                put("device_id", deviceId ?: android.os.Build.ID)
                put("momo_code", AppDefaults.MOMO_CODE)
                userId?.let { put("user_id", it) }
                put("created_at", java.time.Instant.now().toString())
                put("updated_at", java.time.Instant.now().toString())
            }
            
            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", AppDefaults.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${AppDefaults.SUPABASE_ANON_KEY}")
                .addHeader("Prefer", "return=minimal")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful || response.code == 409) { // 409 = already exists (upsert)
                Log.d(TAG, "FCM token registered successfully")
                Result.Success(Unit)
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to register FCM token: HTTP ${response.code} - $errorBody")
                
                // Fallback: Try using edge function if direct table access fails
                if (response.code == 404) {
                    registerTokenViaEdgeFunction(fcmToken, deviceId, userId)
                } else {
                    Result.Error(
                        message = "HTTP ${response.code}: $errorBody",
                        exception = Exception("HTTP ${response.code}: $errorBody")
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while registering FCM token", e)
            // Try fallback method
            registerTokenViaEdgeFunction(fcmToken, deviceId, userId)
        }
    }
    
    /**
     * Fallback: Register token via edge function.
     */
    private suspend fun registerTokenViaEdgeFunction(
        fcmToken: String,
        deviceId: String?,
        userId: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "${AppDefaults.SUPABASE_URL.removeSuffix("/")}/functions/v1/register-fcm-token"
            
            val payload = JSONObject().apply {
                put("fcm_token", fcmToken)
                put("device_id", deviceId ?: android.os.Build.ID)
                put("momo_code", AppDefaults.MOMO_CODE)
                userId?.let { put("user_id", it) }
            }
            
            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", AppDefaults.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${AppDefaults.SUPABASE_ANON_KEY}")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                Log.d(TAG, "FCM token registered via edge function")
                Result.Success(Unit)
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to register FCM token via edge function: HTTP ${response.code} - $errorBody")
                Result.Error(
                    message = "HTTP ${response.code}: $errorBody",
                    exception = Exception("HTTP ${response.code}: $errorBody")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while registering FCM token via edge function", e)
            Result.Error(
                message = "Exception: ${e.message ?: "Unknown error"}",
                exception = e
            )
        }
    }
    
    /**
     * Unregister FCM token (when user logs out or app is uninstalled).
     */
    suspend fun unregisterToken(fcmToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "${AppDefaults.SUPABASE_URL.removeSuffix("/")}/rest/v1/fcm_tokens?fcm_token=eq.$fcmToken"
            
            val request = Request.Builder()
                .url(url)
                .delete()
                .addHeader("apikey", AppDefaults.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${AppDefaults.SUPABASE_ANON_KEY}")
                .addHeader("Prefer", "return=minimal")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                Log.d(TAG, "FCM token unregistered successfully")
                Result.Success(Unit)
            } else {
                Log.w(TAG, "Failed to unregister FCM token: HTTP ${response.code}")
                // Don't fail - token might not exist
                Result.Success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while unregistering FCM token", e)
            Result.Error(
                message = "Exception: ${e.message ?: "Unknown error"}",
                exception = e
            )
        }
    }
}
