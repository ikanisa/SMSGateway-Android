package com.example.smsgateway

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    
    private val _logs = MutableLiveData<String>()
    val logs: LiveData<String> = _logs
    
    private val _smsCount = MutableLiveData<Int>(0)
    val smsCount: LiveData<Int> = _smsCount
    
    private val _errorCount = MutableLiveData<Int>(0)
    val errorCount: LiveData<Int> = _errorCount
    
    private val _isListening = MutableLiveData<Boolean>(false)
    val isListening: LiveData<Boolean> = _isListening
    
    private var logsBuffer = StringBuilder()
    
    fun appendLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val logEntry = "[$timestamp] $message\n"
        logsBuffer.append(logEntry)
        _logs.postValue(logsBuffer.toString())
    }
    
    fun incrementSmsCount() {
        _smsCount.postValue((_smsCount.value ?: 0) + 1)
    }
    
    fun incrementErrorCount() {
        _errorCount.postValue((_errorCount.value ?: 0) + 1)
    }
    
    fun setListening(isListening: Boolean) {
        _isListening.postValue(isListening)
    }
    
    fun clearLogs() {
        logsBuffer.clear()
        _logs.postValue("")
    }
}

