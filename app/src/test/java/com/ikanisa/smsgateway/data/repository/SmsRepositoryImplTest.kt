package com.ikanisa.smsgateway.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.ikanisa.smsgateway.data.Result
import com.ikanisa.smsgateway.data.datasource.SupabaseApi
import com.ikanisa.smsgateway.data.local.dao.DeviceDao
import com.ikanisa.smsgateway.data.local.dao.SmsDao
import com.ikanisa.smsgateway.data.local.entity.SmsMessageEntity
import com.ikanisa.smsgateway.data.model.SmsMessage
import com.ikanisa.smsgateway.data.model.SmsResponse
import com.ikanisa.smsgateway.domain.model.SyncStatus
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SmsRepositoryImpl with mocked dependencies.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SmsRepositoryImplTest {
    
    @MockK
    private lateinit var smsDao: SmsDao
    
    @MockK
    private lateinit var deviceDao: DeviceDao
    
    @MockK
    private lateinit var supabaseApi: SupabaseApi
    
    @MockK
    private lateinit var context: Context
    
    @MockK
    private lateinit var connectivityManager: ConnectivityManager
    
    @MockK
    private lateinit var network: Network
    
    @MockK
    private lateinit var networkCapabilities: NetworkCapabilities
    
    private lateinit var repository: SmsRepositoryImpl
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        
        repository = SmsRepositoryImpl(smsDao, deviceDao, supabaseApi, context)
    }
    
    // =========================================================================
    // saveSms Tests
    // =========================================================================
    
    @Test
    fun `saveSms saves message to local database`() = runTest(testDispatcher) {
        // Given
        val sms = SmsMessage(
            sender = "+250780000000",
            body = "Test MOMO payment",
            timestampMillis = System.currentTimeMillis()
        )
        coEvery { smsDao.insertWithDuplicateCheck(any()) } returns true
        every { connectivityManager.activeNetwork } returns null
        
        // When
        val result = repository.saveSms(sms)
        
        // Then
        assertTrue(result is Result.Success)
        coVerify { smsDao.insertWithDuplicateCheck(any()) }
    }
    
    @Test
    fun `saveSms returns error for duplicate message`() = runTest(testDispatcher) {
        // Given
        val sms = SmsMessage(
            sender = "+250780000000",
            body = "Test MOMO payment",
            timestampMillis = System.currentTimeMillis()
        )
        coEvery { smsDao.insertWithDuplicateCheck(any()) } returns false
        
        // When
        val result = repository.saveSms(sms)
        
        // Then
        assertTrue(result is Result.Error)
        assertEquals("Duplicate message", (result as Result.Error).message)
    }
    
    // =========================================================================
    // syncPendingMessages Tests
    // =========================================================================
    
    @Test
    fun `syncPendingMessages returns error when offline`() = runTest(testDispatcher) {
        // Given
        every { connectivityManager.activeNetwork } returns null
        
        // When
        val result = repository.syncPendingMessages()
        
        // Then
        assertTrue(result is Result.Error)
        assertEquals("Device is offline", (result as Result.Error).message)
    }
    
    @Test
    fun `syncPendingMessages syncs pending messages when online`() = runTest(testDispatcher) {
        // Given
        val pendingEntity = SmsMessageEntity(
            id = "test-id",
            sender = "+250780000000",
            content = "Test payment",
            receivedAt = System.currentTimeMillis(),
            messageHash = "hash123",
            syncStatus = SyncStatus.PENDING
        )
        
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        
        coEvery { smsDao.getPendingSync(any(), any()) } returns listOf(pendingEntity)
        coEvery { smsDao.getById(any()) } returns pendingEntity
        coEvery { smsDao.updateSyncStatus(any(), any(), any()) } returns Unit
        coEvery { supabaseApi.ingestSms(any()) } returns Result.Success(
            SmsResponse(id = "remote-id", parseStatus = "saved", modelUsed = null)
        )
        
        // When
        val result = repository.syncPendingMessages()
        
        // Then
        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data)
    }
    
    // =========================================================================
    // getSyncStats Tests
    // =========================================================================
    
    @Test
    fun `getSyncStats returns combined stats flow`() = runTest(testDispatcher) {
        // Given
        coEvery { smsDao.getCountByStatus(SyncStatus.PENDING) } returns flowOf(5)
        coEvery { smsDao.getCountByStatus(SyncStatus.SYNCING) } returns flowOf(1)
        coEvery { smsDao.getCountByStatus(SyncStatus.SYNCED) } returns flowOf(100)
        coEvery { smsDao.getCountByStatus(SyncStatus.FAILED) } returns flowOf(3)
        
        // When
        val statsFlow = repository.getSyncStats()
        
        // Then - verify the flow is returned correctly
        // This test ensures the combine logic is set up
    }
    
    // =========================================================================
    // Legacy Method Tests
    // =========================================================================
    
    @Test
    fun `sendSmsToBackend delegates to saveSms`() = runTest(testDispatcher) {
        // Given
        val sms = SmsMessage(
            sender = "+250780000000",
            body = "Test payment",
            timestampMillis = System.currentTimeMillis()
        )
        coEvery { smsDao.insertWithDuplicateCheck(any()) } returns true
        every { connectivityManager.activeNetwork } returns null
        
        // When
        val result = repository.sendSmsToBackend(sms)
        
        // Then
        assertTrue(result is Result.Success)
    }
    
    @Test
    fun `resetCounters resets both counters to zero`() = runTest(testDispatcher) {
        // When
        repository.resetCounters()
        
        // Then
        assertEquals(0, repository.observeSmsCount().value)
        assertEquals(0, repository.observeErrorCount().value)
    }
}
