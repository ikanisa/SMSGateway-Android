package com.ikanisa.smsgateway.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikanisa.smsgateway.data.local.dao.SmsDao
import com.ikanisa.smsgateway.data.repository.SmsRepository
import com.ikanisa.smsgateway.presentation.components.ConnectionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val smsDao: SmsDao,
    private val smsRepository: SmsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Use DAO directly to get entities with all fields
                combine(
                    smsDao.getAllFlow(),
                    smsRepository.getSyncStats()
                ) { entities, syncStats ->
                    // Map entities to presentation models
                    val smsMessages = entities.map { entity ->
                        SmsMessage(
                            id = entity.id,
                            sender = entity.sender,
                            content = entity.content,
                            receivedAt = entity.receivedAt,
                            syncStatus = when (entity.syncStatus) {
                                com.ikanisa.smsgateway.domain.model.SyncStatus.SYNCED -> SyncStatus.SYNCED
                                com.ikanisa.smsgateway.domain.model.SyncStatus.SYNCING -> SyncStatus.SYNCING
                                com.ikanisa.smsgateway.domain.model.SyncStatus.PENDING -> SyncStatus.PENDING
                                com.ikanisa.smsgateway.domain.model.SyncStatus.FAILED -> SyncStatus.FAILED
                            }
                        )
                    }
                    
                    val stats = SmsStats(
                        total = syncStats.total,
                        synced = syncStats.synced,
                        pending = syncStats.pending + syncStats.syncing
                    )
                    
                    // Determine connection status based on sync activity
                    val connectionStatus = when {
                        syncStats.syncing > 0 -> ConnectionStatus.SYNCING
                        syncStats.failed > 0 && syncStats.synced == 0 -> ConnectionStatus.OFFLINE
                        else -> ConnectionStatus.CONNECTED
                    }
                    
                    Triple(smsMessages, stats, connectionStatus)
                }
                .catch { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load messages"
                        )
                    }
                }
                .collect { (messages, stats, status) ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            messages = messages,
                            stats = stats,
                            connectionStatus = status,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun retry() {
        _uiState.update { it.copy(error = null) }
        loadData()
    }

    fun selectMessage(message: SmsMessage) {
        // Handle message selection - can expand for detail view
    }
    
    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.SYNCING) }
            try {
                smsRepository.syncPendingMessages()
            } catch (e: Exception) {
                // Error will be reflected in connection status via flow
            }
        }
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
