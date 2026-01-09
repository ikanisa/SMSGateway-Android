package com.example.smsgateway

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smsgateway.data.repository.SmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel for the main screen.
 * Uses Repository pattern for data operations.
 * Injected via Hilt.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: SmsRepository
) : ViewModel() {
    
    // Observe SMS and error counts from repository
    val smsCount: StateFlow<Int> = repository.observeSmsCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    val errorCount: StateFlow<Int> = repository.observeErrorCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    // Logs are still managed locally (could be moved to Repository if needed)
    private val _logs = MutableLiveData<String>()
    val logs: LiveData<String> = _logs
    
    private val _isListening = MutableLiveData<Boolean>(false)
    val isListening: LiveData<Boolean> = _isListening
    
    // Keep last 100 log entries to prevent memory issues
    private val logsBuffer = mutableListOf<String>()
    private val maxLogEntries = 100
    
    fun appendLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            .format(Date())
        val logEntry = "[$timestamp] $message"
        
        logsBuffer.add(logEntry)
        
        // Keep only last N entries
        if (logsBuffer.size > maxLogEntries) {
            logsBuffer.removeAt(0)
        }
        
        _logs.postValue(logsBuffer.joinToString("\n"))
    }
    
    fun setListening(isListening: Boolean) {
        _isListening.postValue(isListening)
    }
    
    fun clearLogs() {
        logsBuffer.clear()
        _logs.postValue("")
    }
    
    /**
     * Reset SMS and error counters.
     */
    fun resetCounters() {
        viewModelScope.launch {
            repository.resetCounters()
        }
    }
}

