package com.ikanisa.smsgateway.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikanisa.smsgateway.presentation.components.ConnectionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Simulation of initial data load
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1000) // Simulate network/db delay
            val mockMessages = generateMockMessages()
            val mockStats = SmsStats(total = 124, synced = 118, pending = 6)
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    messages = mockMessages,
                    stats = mockStats,
                    connectionStatus = ConnectionStatus.CONNECTED
                )
            }
        }
    }

    fun retry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            delay(1000)
            // Retry logic here
             _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun selectMessage(message: SmsMessage) {
        // Handle message selection
    }
    
    // Mock data generation
    private fun generateMockMessages(): List<SmsMessage> {
        return listOf(
             SmsMessage(
                id = UUID.randomUUID().toString(),
                sender = "+250788123456",
                content = "Payment received successfully. Ref: 123456789",
                receivedAt = System.currentTimeMillis() - 5000,
                syncStatus = SyncStatus.SYNCED
            ),
            SmsMessage(
                id = UUID.randomUUID().toString(),
                sender = "MTN MobileMoney",
                content = "You have received 5000 RWF from John Doe.",
                receivedAt = System.currentTimeMillis() - 1000 * 60 * 15, // 15 mins ago
                syncStatus = SyncStatus.PENDING
            ),
             SmsMessage(
                id = UUID.randomUUID().toString(),
                sender = "Airtel Money",
                content = "Transaction failed. Insufficient funds.",
                receivedAt = System.currentTimeMillis() - 1000 * 60 * 60, // 1 hour ago
                syncStatus = SyncStatus.FAILED
            )
        )
    }
}

data class SmsStats(
    val total: Int = 0,
    val synced: Int = 0,
    val pending: Int = 0
)

enum class SyncStatus {
    SYNCED, SYNCING, PENDING, FAILED
}

data class SmsMessage(
    val id: String,
    val sender: String,
    val content: String,
    val receivedAt: Long,
    val syncStatus: SyncStatus
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val messages: List<SmsMessage> = emptyList(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.OFFLINE,
    val stats: SmsStats = SmsStats()
)
