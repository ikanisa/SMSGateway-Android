package com.ikanisa.smsgateway.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import com.ikanisa.smsgateway.AppDefaults
import com.ikanisa.smsgateway.data.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ActivationRepository using Supabase for backend
 * and SharedPreferences for local activation state.
 */
@Singleton
class ActivationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabaseClient: SupabaseClient
) : ActivationRepository {
    
    companion object {
        private const val PREFS_NAME = "sms_gateway_activation"
        private const val KEY_IS_ACTIVATED = "is_activated"
        private const val KEY_MOMO_CODE = "momo_code"
        private const val KEY_ACTIVATION_TIMESTAMP = "activation_timestamp"
        private const val TABLE_APP_DEVICES = "app_devices"
    }
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Device model representing a registered app device in Supabase.
     */
    @Serializable
    data class AppDevice(
        val id: String? = null,
        val momo_code: String,
        val device_id: String? = null,
        val is_activated: Boolean = false,
        val staff_name: String? = null,
        val created_at: String? = null,
        val activated_at: String? = null
    )
    
    override suspend fun validateMomoCode(momoCode: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Query Supabase for device with matching MOMO code
            val devices = supabaseClient
                .from(TABLE_APP_DEVICES)
                .select {
                    filter {
                        eq("momo_code", momoCode)
                    }
                }
                .decodeList<AppDevice>()
            
            if (devices.isEmpty()) {
                Result.Success(false) // MOMO code not found
            } else {
                val device = devices.first()
                // Check if already activated by another device
                if (device.is_activated && device.device_id != getDeviceId()) {
                    Result.Error("This MOMO code is already activated on another device")
                } else {
                    Result.Success(true) // Valid and available for activation
                }
            }
        } catch (e: Exception) {
            // If table doesn't exist or network error, fall back to local validation
            // For now, accept the MOMO code if it matches the one in BuildConfig
            val buildConfigMomo = AppDefaults.MOMO_CODE
            if (buildConfigMomo.isNotEmpty() && momoCode == buildConfigMomo) {
                Result.Success(true)
            } else if (momoCode.length >= 4) {
                // Accept any MOMO code with minimum length for offline mode
                Result.Success(true)
            } else {
                Result.Error("Invalid MOMO code format")
            }
        }
    }
    
    override suspend fun activateDevice(momoCode: String, deviceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Update device record in Supabase
            supabaseClient
                .from(TABLE_APP_DEVICES)
                .update({
                    set("device_id", deviceId)
                    set("is_activated", true)
                    set("activated_at", java.time.Instant.now().toString())
                }) {
                    filter {
                        eq("momo_code", momoCode)
                    }
                }
            
            // Store activation state locally
            storeActivationState(momoCode, true)
            Result.Success(Unit)
        } catch (e: Exception) {
            // Even if Supabase update fails, store locally for offline operation
            storeActivationState(momoCode, true)
            Result.Success(Unit)
        }
    }
    
    override suspend fun isDeviceActivated(): Boolean {
        return prefs.getBoolean(KEY_IS_ACTIVATED, false)
    }
    
    override fun getStoredMomoCode(): String? {
        return prefs.getString(KEY_MOMO_CODE, null)
    }
    
    override suspend fun storeActivationState(momoCode: String, isActivated: Boolean) {
        prefs.edit()
            .putBoolean(KEY_IS_ACTIVATED, isActivated)
            .putString(KEY_MOMO_CODE, momoCode)
            .putLong(KEY_ACTIVATION_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }
    
    private fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
    }
}
