package com.example.smsgateway.data.repository

import android.content.Context
import com.example.smsgateway.AppDefaults
import com.example.smsgateway.data.Result
import com.example.smsgateway.data.datasource.SupabaseApi
import com.example.smsgateway.data.model.SmsMessage
import com.example.smsgateway.data.model.SmsResponse
import com.example.smsgateway.data.SecurePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementation of SmsRepository.
 * Manages SMS forwarding to Supabase and maintains counters.
 */
class SmsRepositoryImpl(
    private val context: Context
) : SmsRepository {
    
    private val prefs = SecurePreferences(context)
    
    private val _smsCount = MutableStateFlow(0)
    override fun observeSmsCount(): StateFlow<Int> = _smsCount.asStateFlow()
    
    private val _errorCount = MutableStateFlow(0)
    override fun observeErrorCount(): StateFlow<Int> = _errorCount.asStateFlow()
    
    override suspend fun getSmsCount(): Int = _smsCount.value
    override suspend fun getErrorCount(): Int = _errorCount.value
    
    override suspend fun resetCounters() {
        _smsCount.value = 0
        _errorCount.value = 0
    }
    
    override suspend fun sendSmsToBackend(sms: SmsMessage): Result<SmsResponse> {
        // Load configuration from secure preferences
        val supabaseUrl = prefs.getSupabaseUrl()?.trim() ?: AppDefaults.SUPABASE_URL.trim()
        val supabaseAnon = prefs.getSupabaseKey()?.trim() ?: AppDefaults.SUPABASE_ANON_KEY.trim()
        val deviceId = prefs.getDeviceId()?.trim().orEmpty()
        val deviceSecret = prefs.getDeviceSecret()?.trim().orEmpty()
        val deviceLabel = prefs.getDeviceLabel()?.trim()
        val momoMsisdn = prefs.getMomoMsisdn()?.trim()
        val momoCode = prefs.getMomoCode()?.trim()
        
        // Validate configuration
        if (supabaseUrl.isEmpty() || supabaseAnon.isEmpty() || deviceId.isEmpty() || deviceSecret.isEmpty()) {
            return Result.Error(
                message = "Not configured: open Settings and Save Supabase + Device config",
                exception = IllegalStateException("Missing required configuration")
            )
        }
        
        // Create API client with current configuration
        val api = SupabaseApi(
            supabaseUrl = supabaseUrl,
            supabaseAnonKey = supabaseAnon,
            deviceId = deviceId,
            deviceSecret = deviceSecret,
            deviceLabel = deviceLabel,
            momoMsisdn = momoMsisdn,
            momoCode = momoCode
        )
        
        // Send SMS and update counters
        return when (val result = api.ingestSms(sms)) {
            is Result.Success -> {
                _smsCount.value = _smsCount.value + 1
                result
            }
            is Result.Error -> {
                _errorCount.value = _errorCount.value + 1
                result
            }
        }
    }
}
