package com.example.smsgateway

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Helper class to look up device credentials from Supabase by MOMO number.
 */
class DeviceLookupHelper(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("SMSGatewayPrefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    data class DeviceInfo(
        val deviceId: String,
        val deviceSecret: String,
        val deviceLabel: String,
        val momoMsisdn: String,
        val momoCode: String?,
        val supabaseUrl: String
    )

    sealed class LookupResult {
        data class Success(val deviceInfo: DeviceInfo) : LookupResult()
        data class Error(val message: String) : LookupResult()
    }

    /**
     * Look up device by MOMO number and save credentials locally.
     */
    suspend fun lookupAndSaveDevice(momoMsisdn: String, momoCode: String): LookupResult = withContext(Dispatchers.IO) {
        try {
            val url = "${AppDefaults.SUPABASE_URL}/functions/v1/lookup-device"
            
            val payload = JSONObject().apply {
                put("momo_msisdn", momoMsisdn)
            }

            val req = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", AppDefaults.SUPABASE_ANON_KEY)
                .build()

            client.newCall(req).execute().use { resp ->
                val respText = resp.body?.string().orEmpty()

                if (!resp.isSuccessful) {
                    val errorJson = try { JSONObject(respText) } catch (_: Exception) { null }
                    val errorMsg = errorJson?.optString("message") 
                        ?: errorJson?.optString("error") 
                        ?: "Device lookup failed (${resp.code})"
                    return@withContext LookupResult.Error(errorMsg)
                }

                val json = JSONObject(respText)
                
                if (!json.optBoolean("ok", false)) {
                    return@withContext LookupResult.Error(json.optString("error", "Unknown error"))
                }

                val deviceInfo = DeviceInfo(
                    deviceId = json.getString("device_id"),
                    deviceSecret = json.getString("device_secret"),
                    deviceLabel = json.optString("device_label", ""),
                    momoMsisdn = json.optString("momo_msisdn", momoMsisdn),
                    momoCode = momoCode, // Use user-provided code
                    supabaseUrl = json.optString("supabase_url", AppDefaults.SUPABASE_URL)
                )

                // Save to SharedPreferences
                prefs.edit()
                    .putString("supabase_url", deviceInfo.supabaseUrl)
                    .putString("supabase_key", AppDefaults.SUPABASE_ANON_KEY)
                    .putString("device_id", deviceInfo.deviceId)
                    .putString("device_secret", deviceInfo.deviceSecret)
                    .putString("device_label", deviceInfo.deviceLabel)
                    .putString("momo_msisdn", deviceInfo.momoMsisdn)
                    .putString("momo_code", momoCode)
                    .putBoolean("is_provisioned", true)
                    .apply()

                return@withContext LookupResult.Success(deviceInfo)
            }
        } catch (e: Exception) {
            return@withContext LookupResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Check if device is already provisioned.
     */
    fun isProvisioned(): Boolean {
        return prefs.getBoolean("is_provisioned", false) &&
               prefs.getString("device_id", "")?.isNotEmpty() == true
    }

    /**
     * Get current device info from SharedPreferences.
     */
    fun getStoredDeviceInfo(): DeviceInfo? {
        val deviceId = prefs.getString("device_id", "") ?: return null
        if (deviceId.isEmpty()) return null

        return DeviceInfo(
            deviceId = deviceId,
            deviceSecret = prefs.getString("device_secret", "") ?: "",
            deviceLabel = prefs.getString("device_label", "") ?: "",
            momoMsisdn = prefs.getString("momo_msisdn", "") ?: "",
            momoCode = prefs.getString("momo_code", ""),
            supabaseUrl = prefs.getString("supabase_url", AppDefaults.SUPABASE_URL) ?: AppDefaults.SUPABASE_URL
        )
    }

    /**
     * Clear all stored credentials.
     */
    fun clearCredentials() {
        prefs.edit()
            .remove("device_id")
            .remove("device_secret")
            .remove("device_label")
            .remove("is_provisioned")
            .apply()
    }
}
