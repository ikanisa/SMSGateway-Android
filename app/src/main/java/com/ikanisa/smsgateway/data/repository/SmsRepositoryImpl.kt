package com.ikanisa.smsgateway.data.repository

import com.ikanisa.smsgateway.data.Result
import com.ikanisa.smsgateway.data.datasource.SupabaseApi
import com.ikanisa.smsgateway.data.model.SmsMessage
import com.ikanisa.smsgateway.data.model.SmsResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simplified repository - direct SMS forwarding with counters.
 */
class SmsRepositoryImpl : SmsRepository {
    
    private val api = SupabaseApi()
    
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
