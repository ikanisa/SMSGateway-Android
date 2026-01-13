package com.ikanisa.smsgateway.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.ikanisa.smsgateway.data.Result
import com.ikanisa.smsgateway.data.datasource.SupabaseApi
import com.ikanisa.smsgateway.data.local.dao.DeviceDao
import com.ikanisa.smsgateway.data.local.dao.SmsDao
import com.ikanisa.smsgateway.data.mapper.toDomain
import com.ikanisa.smsgateway.data.mapper.toDomainList
import com.ikanisa.smsgateway.data.mapper.toEntity
import com.ikanisa.smsgateway.data.model.SmsMessage
import com.ikanisa.smsgateway.data.model.SmsResponse
import com.ikanisa.smsgateway.domain.exception.DuplicateMessageException
import com.ikanisa.smsgateway.domain.model.SyncStats
import com.ikanisa.smsgateway.domain.model.SyncStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first SMS repository implementation.
 * 
 * Features:
 * - Local storage with Room database
 * - Deduplication via message hash
 * - Automatic sync when online
 * - Retry queue for failed syncs
 * - Sync statistics tracking
 */
@Singleton
class SmsRepositoryImpl @Inject constructor(
    private val smsDao: SmsDao,
    private val deviceDao: DeviceDao,
    private val supabaseApi: SupabaseApi,
    @ApplicationContext private val context: Context
) : SmsRepository {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    // Counters for backward compatibility
    private val _smsCount = MutableStateFlow(0)
    private val _errorCount = MutableStateFlow(0)
    
    // =========================================================================
    // Offline-First Operations
    // =========================================================================
    
    override suspend fun saveSms(sms: SmsMessage): Result<String> = withContext(Dispatchers.IO) {
        try {
            val entity = sms.toEntity()
            val inserted = smsDao.insertWithDuplicateCheck(entity)
            
            if (!inserted) {
                Timber.d("Duplicate message detected, hash: ${entity.messageHash.take(16)}...")
                return@withContext Result.Error(
                    message = "Duplicate message",
                    exception = DuplicateMessageException("Message already exists")
                )
            }
            
            Timber.d("SMS saved locally with ID: ${entity.id}, status: PENDING")
            
            // Attempt immediate sync if online
            if (isOnline()) {
                try {
                    syncSingleMessage(entity.id)
                    Timber.d("SMS synced immediately: ${entity.id}")
                } catch (e: Exception) {
                    Timber.w(e, "Immediate sync failed, will retry later")
                    // Don't fail the save - message is stored locally
                }
            } else {
                Timber.d("Device offline, SMS queued for later sync")
            }
            
            Result.Success(entity.id)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save SMS")
            _errorCount.value++
            Result.Error(message = e.message ?: "Unknown error", exception = e)
        }
    }
    
    override fun getAllMessages(): Flow<List<SmsMessage>> {
        return smsDao.getAllFlow()
            .map { entities -> entities.toDomainList() }
            .flowOn(Dispatchers.IO)
    }
    
    override suspend fun syncPendingMessages(): Result<Int> = withContext(Dispatchers.IO) {
        if (!isOnline()) {
            return@withContext Result.Error(
                message = "Device is offline",
                exception = IllegalStateException("No internet connection")
            )
        }
        
        try {
            val pending = smsDao.getPendingSync(SyncStatus.PENDING, 50)
            var syncedCount = 0
            
            Timber.d("Found ${pending.size} pending messages to sync")
            
            pending.forEach { entity ->
                try {
                    syncSingleMessage(entity.id)
                    syncedCount++
                } catch (e: Exception) {
                    Timber.w(e, "Failed to sync message ${entity.id}")
                    handleSyncFailure(entity.id, e)
                }
            }
            
            Timber.i("Synced $syncedCount of ${pending.size} messages")
            Result.Success(syncedCount)
        } catch (e: Exception) {
            Timber.e(e, "Sync batch failed")
            Result.Error(message = e.message ?: "Sync failed", exception = e)
        }
    }
    
    override fun getSyncStats(): Flow<SyncStats> {
        return combine(
            smsDao.getCountByStatus(SyncStatus.PENDING),
            smsDao.getCountByStatus(SyncStatus.SYNCING),
            smsDao.getCountByStatus(SyncStatus.SYNCED),
            smsDao.getCountByStatus(SyncStatus.FAILED)
        ) { pending, syncing, synced, failed ->
            SyncStats(pending, syncing, synced, failed)
        }.flowOn(Dispatchers.IO)
    }
    
    // =========================================================================
    // Legacy Operations
    // =========================================================================
    
    override suspend fun sendSmsToBackend(sms: SmsMessage): Result<SmsResponse> {
        // Use the new offline-first approach
        return when (val result = saveSms(sms)) {
            is Result.Success -> {
                _smsCount.value++
                Result.Success(SmsResponse(id = result.data, parseStatus = "saved", modelUsed = null))
            }
            is Result.Error -> {
                _errorCount.value++
                Result.Error(message = result.message, exception = result.exception)
            }
        }
    }
    
    override fun observeSmsCount(): StateFlow<Int> = _smsCount.asStateFlow()
    override fun observeErrorCount(): StateFlow<Int> = _errorCount.asStateFlow()
    override suspend fun getSmsCount(): Int = _smsCount.value
    override suspend fun getErrorCount(): Int = _errorCount.value
    
    override suspend fun resetCounters() {
        _smsCount.value = 0
        _errorCount.value = 0
    }
    
    // =========================================================================
    // Private Helpers
    // =========================================================================
    
    private suspend fun syncSingleMessage(messageId: String) {
        val entity = smsDao.getById(messageId) 
            ?: throw IllegalStateException("Message not found: $messageId")
        
        // Mark as syncing
        smsDao.updateSyncStatus(messageId, SyncStatus.SYNCING, null)
        
        // Create domain model and send to API
        val sms = entity.toDomain()
        val result = supabaseApi.ingestSms(sms)
        
        when (result) {
            is Result.Success -> {
                smsDao.updateSyncStatus(
                    id = messageId,
                    status = SyncStatus.SYNCED,
                    syncedAt = System.currentTimeMillis()
                )
                _smsCount.value++
                Timber.d("Message synced successfully: $messageId")
            }
            is Result.Error -> {
                throw result.exception ?: Exception(result.message)
            }
        }
    }
    
    private suspend fun handleSyncFailure(messageId: String, error: Exception) {
        val entity = smsDao.getById(messageId) ?: return
        
        val newStatus = if (entity.retryCount >= 3) SyncStatus.FAILED else SyncStatus.PENDING
        
        smsDao.incrementRetryCount(
            id = messageId,
            error = error.message ?: "Unknown error",
            status = newStatus
        )
        
        if (newStatus == SyncStatus.FAILED) {
            _errorCount.value++
            Timber.w("Message $messageId marked as FAILED after ${entity.retryCount + 1} retries")
        }
    }
    
    private fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
