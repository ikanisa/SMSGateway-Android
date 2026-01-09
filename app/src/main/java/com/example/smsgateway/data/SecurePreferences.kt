package com.example.smsgateway.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure wrapper around EncryptedSharedPreferences for storing sensitive data.
 * Uses Android Keystore to encrypt keys and values at rest.
 * 
 * All sensitive data (device secrets, API keys) should be stored using this class
 * instead of regular SharedPreferences.
 */
class SecurePreferences(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val PREFS_NAME = "SMSGatewayPrefs"
        
        // Preference keys
        const val KEY_SUPABASE_URL = "supabase_url"
        const val KEY_SUPABASE_KEY = "supabase_key"
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_DEVICE_SECRET = "device_secret"
        const val KEY_DEVICE_LABEL = "device_label"
        const val KEY_MOMO_MSISDN = "momo_msisdn"
        const val KEY_MOMO_CODE = "momo_code"
        const val KEY_IS_PROVISIONED = "is_provisioned"
    }
    
    // Supabase configuration
    fun getSupabaseUrl(): String? = encryptedPrefs.getString(KEY_SUPABASE_URL, null)
    fun setSupabaseUrl(url: String) = encryptedPrefs.edit().putString(KEY_SUPABASE_URL, url).apply()
    
    fun getSupabaseKey(): String? = encryptedPrefs.getString(KEY_SUPABASE_KEY, null)
    fun setSupabaseKey(key: String) = encryptedPrefs.edit().putString(KEY_SUPABASE_KEY, key).apply()
    
    // Device credentials
    fun getDeviceId(): String? = encryptedPrefs.getString(KEY_DEVICE_ID, null)
    fun setDeviceId(id: String) = encryptedPrefs.edit().putString(KEY_DEVICE_ID, id).apply()
    
    fun getDeviceSecret(): String? = encryptedPrefs.getString(KEY_DEVICE_SECRET, null)
    fun setDeviceSecret(secret: String) = encryptedPrefs.edit().putString(KEY_DEVICE_SECRET, secret).apply()
    
    fun getDeviceLabel(): String? = encryptedPrefs.getString(KEY_DEVICE_LABEL, null)
    fun setDeviceLabel(label: String) = encryptedPrefs.edit().putString(KEY_DEVICE_LABEL, label).apply()
    
    // MoMo configuration
    fun getMomoMsisdn(): String? = encryptedPrefs.getString(KEY_MOMO_MSISDN, null)
    fun setMomoMsisdn(msisdn: String) = encryptedPrefs.edit().putString(KEY_MOMO_MSISDN, msisdn).apply()
    
    fun getMomoCode(): String? = encryptedPrefs.getString(KEY_MOMO_CODE, null)
    fun setMomoCode(code: String) = encryptedPrefs.edit().putString(KEY_MOMO_CODE, code).apply()
    
    // Provisioning status
    fun isProvisioned(): Boolean = encryptedPrefs.getBoolean(KEY_IS_PROVISIONED, false)
    fun setProvisioned(provisioned: Boolean) = encryptedPrefs.edit().putBoolean(KEY_IS_PROVISIONED, provisioned).apply()
    
    // Generic getters/setters for flexibility
    fun getString(key: String, defaultValue: String?): String? = encryptedPrefs.getString(key, defaultValue)
    fun putString(key: String, value: String) = encryptedPrefs.edit().putString(key, value).apply()
    
    fun getBoolean(key: String, defaultValue: Boolean): Boolean = encryptedPrefs.getBoolean(key, defaultValue)
    fun putBoolean(key: String, value: Boolean) = encryptedPrefs.edit().putBoolean(key, value).apply()
    
    fun getInt(key: String, defaultValue: Int): Int = encryptedPrefs.getInt(key, defaultValue)
    fun putInt(key: String, value: Int) = encryptedPrefs.edit().putInt(key, value).apply()
    
    // Clear all preferences
    fun clear() = encryptedPrefs.edit().clear().apply()
    
    // Remove specific key
    fun remove(key: String) = encryptedPrefs.edit().remove(key).apply()
    
    // Check if key exists
    fun contains(key: String): Boolean = encryptedPrefs.contains(key)
    
    // Get all keys
    fun getAll(): Map<String, *> = encryptedPrefs.all
}
