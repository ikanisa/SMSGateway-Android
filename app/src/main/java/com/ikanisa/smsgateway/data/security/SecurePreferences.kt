package com.ikanisa.smsgateway.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure storage for sensitive data using Android's EncryptedSharedPreferences.
 * 
 * Manages:
 * - Database encryption key (for SQLCipher)
 * - Supabase credentials
 * - Device authentication tokens
 * - Biometric settings
 * - Other sensitive configuration
 */
@Singleton
class SecurePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    // ==================== Database Key ====================
    
    /**
     * Get the database encryption key, generating one if it doesn't exist.
     */
    fun getDatabaseKey(): String {
        return encryptedPrefs.getString(KEY_DATABASE_PASSPHRASE, null)
            ?: generateAndStoreDatabaseKey()
    }
    
    /**
     * Generate and store a new database encryption key.
     */
    private fun generateAndStoreDatabaseKey(): String {
        val keyBytes = ByteArray(32) // 256 bits
        SecureRandom().nextBytes(keyBytes)
        val key = Base64.getEncoder().encodeToString(keyBytes)
        
        encryptedPrefs.edit()
            .putString(KEY_DATABASE_PASSPHRASE, key)
            .apply()
        
        return key
    }
    
    // ==================== Supabase Credentials ====================
    
    fun setSupabaseUrl(url: String) {
        encryptedPrefs.edit().putString(KEY_SUPABASE_URL, url).apply()
    }
    
    fun getSupabaseUrl(): String? {
        return encryptedPrefs.getString(KEY_SUPABASE_URL, null)
    }
    
    fun setSupabaseKey(key: String) {
        encryptedPrefs.edit().putString(KEY_SUPABASE_KEY, key).apply()
    }
    
    fun getSupabaseKey(): String? {
        return encryptedPrefs.getString(KEY_SUPABASE_KEY, null)
    }
    
    // ==================== Device Authentication ====================
    
    fun setDeviceSecret(secret: String) {
        encryptedPrefs.edit().putString(KEY_DEVICE_SECRET, secret).apply()
    }
    
    fun getDeviceSecret(): String? {
        return encryptedPrefs.getString(KEY_DEVICE_SECRET, null)
    }
    
    fun setDeviceId(id: String) {
        encryptedPrefs.edit().putString(KEY_DEVICE_ID, id).apply()
    }
    
    fun getDeviceId(): String? {
        return encryptedPrefs.getString(KEY_DEVICE_ID, null)
    }
    
    // ==================== Security Settings ====================
    
    fun setBiometricEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }
    
    fun isBiometricEnabled(): Boolean {
        return encryptedPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
    
    // ==================== Generic Accessors ====================
    
    /**
     * Store a secure string value.
     */
    fun putString(key: String, value: String) {
        encryptedPrefs.edit()
            .putString(key, value)
            .apply()
    }
    
    /**
     * Retrieve a secure string value.
     */
    fun getString(key: String, defaultValue: String? = null): String? {
        return encryptedPrefs.getString(key, defaultValue)
    }
    
    /**
     * Store a secure boolean value.
     */
    fun putBoolean(key: String, value: Boolean) {
        encryptedPrefs.edit()
            .putBoolean(key, value)
            .apply()
    }
    
    /**
     * Retrieve a secure boolean value.
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return encryptedPrefs.getBoolean(key, defaultValue)
    }
    
    /**
     * Remove a secure value.
     */
    fun remove(key: String) {
        encryptedPrefs.edit()
            .remove(key)
            .apply()
    }
    
    /**
     * Clear all secure values (for logout/reset).
     */
    fun clearAll() {
        encryptedPrefs.edit()
            .clear()
            .apply()
    }
    
    companion object {
        private const val PREFS_FILE_NAME = "sms_gateway_secure_prefs"
        
        // Database
        private const val KEY_DATABASE_PASSPHRASE = "db_passphrase"
        
        // Supabase
        private const val KEY_SUPABASE_URL = "supabase_url"
        private const val KEY_SUPABASE_KEY = "supabase_key"
        
        // Device Authentication
        private const val KEY_DEVICE_SECRET = "device_secret"
        private const val KEY_DEVICE_ID = "device_id"
        
        // Security Settings
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }
}
