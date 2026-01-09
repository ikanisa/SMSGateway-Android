package com.example.smsgateway

import android.content.Context
import com.example.smsgateway.data.SecurePreferences
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

    private val prefs = SecurePreferences(context)

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

                // Save to SecurePreferences (encrypted storage)
                prefs.setSupabaseUrl(deviceInfo.supabaseUrl)
                prefs.setSupabaseKey(AppDefaults.SUPABASE_ANON_KEY)
                prefs.setDeviceId(deviceInfo.deviceId)
                prefs.setDeviceSecret(deviceInfo.deviceSecret)
                prefs.setDeviceLabel(deviceInfo.deviceLabel)
                prefs.setMomoMsisdn(deviceInfo.momoMsisdn)
                prefs.setMomoCode(momoCode)
                prefs.setProvisioned(true)

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
        return prefs.isProvisioned() && prefs.getDeviceId()?.isNotEmpty() == true
    }

    /**
     * Get current device info from SecurePreferences.
     */
    fun getStoredDeviceInfo(): DeviceInfo? {
        val deviceId = prefs.getDeviceId() ?: return null
        if (deviceId.isEmpty()) return null

        return DeviceInfo(
            deviceId = deviceId,
            deviceSecret = prefs.getDeviceSecret() ?: "",
            deviceLabel = prefs.getDeviceLabel() ?: "",
            momoMsisdn = prefs.getMomoMsisdn() ?: "",
            momoCode = prefs.getMomoCode(),
            supabaseUrl = prefs.getSupabaseUrl() ?: AppDefaults.SUPABASE_URL
        )
    }

    /**
     * Clear all stored credentials.
     */
    fun clearCredentials() {
        prefs.remove(SecurePreferences.KEY_DEVICE_ID)
        prefs.remove(SecurePreferences.KEY_DEVICE_SECRET)
        prefs.remove(SecurePreferences.KEY_DEVICE_LABEL)
        prefs.setProvisioned(false)
    }
}
