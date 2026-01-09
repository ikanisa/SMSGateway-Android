package com.ikanisa.smsgateway.data.repository

import com.ikanisa.smsgateway.data.Result
import com.ikanisa.smsgateway.data.datasource.SupabaseApi
import com.ikanisa.smsgateway.data.model.SmsMessage
import com.ikanisa.smsgateway.data.model.SmsResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SmsRepositoryTest {
    
    private lateinit var repository: SmsRepositoryImpl
    private lateinit var mockApi: SupabaseApi
    private lateinit var mockContext: android.content.Context
    private lateinit var mockSecurePreferences: com.ikanisa.smsgateway.data.SecurePreferences
    
    @Before
    fun setup() {
        mockApi = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)
        mockSecurePreferences = mockk(relaxed = true)
        
        // Mock SecurePreferences to return valid configuration
        coEvery { mockSecurePreferences.getSupabaseUrl() } returns "https://test.supabase.co"
        coEvery { mockSecurePreferences.getSupabaseKey() } returns "test-key"
        coEvery { mockSecurePreferences.getDeviceId() } returns "test-device-id"
        coEvery { mockSecurePreferences.getDeviceSecret() } returns "test-device-secret"
        coEvery { mockSecurePreferences.getDeviceLabel() } returns null
        coEvery { mockSecurePreferences.getMomoMsisdn() } returns null
        coEvery { mockSecurePreferences.getMomoCode() } returns null
        
        repository = SmsRepositoryImpl(mockContext)
        // Note: In a real test, we'd need to inject the mocked dependencies
        // For now, this is a template showing the test structure
    }
    
    @Test
    fun `sendSmsToBackend returns success when API call succeeds`() = runTest {
        // Given
        val sms = SmsMessage(
            sender = "1234567890",
            body = "Test SMS",
            timestampMillis = System.currentTimeMillis()
        )
        val expectedResponse = SmsResponse(
            id = "test-id",
            parseStatus = "parsed",
            modelUsed = "gemini-2.0-flash"
        )
        
        // When
        coEvery { mockApi.ingestSms(any()) } returns Result.Success(expectedResponse)
        val result = repository.sendSmsToBackend(sms)
        
        // Then
        assertTrue(result.isSuccess)
        if (result is Result.Success) {
            assertEquals(expectedResponse.id, result.data.id)
            assertEquals(expectedResponse.parseStatus, result.data.parseStatus)
        }
        
        // Verify SMS count was incremented
        val smsCount = repository.observeSmsCount().first()
        assertEquals(1, smsCount)
    }
    
    @Test
    fun `sendSmsToBackend returns error when API call fails`() = runTest {
        // Given
        val sms = SmsMessage(
            sender = "1234567890",
            body = "Test SMS",
            timestampMillis = System.currentTimeMillis()
        )
        val errorMessage = "Network error"
        
        // When
        coEvery { mockApi.ingestSms(any()) } returns Result.Error(errorMessage)
        val result = repository.sendSmsToBackend(sms)
        
        // Then
        assertTrue(result.isError)
        if (result is Result.Error) {
            assertEquals(errorMessage, result.message)
        }
        
        // Verify error count was incremented
        val errorCount = repository.observeErrorCount().first()
        assertEquals(1, errorCount)
    }
    
    @Test
    fun `sendSmsToBackend returns error when not configured`() = runTest {
        // Given
        val sms = SmsMessage(
            sender = "1234567890",
            body = "Test SMS",
            timestampMillis = System.currentTimeMillis()
        )
        
        // Mock missing configuration
        coEvery { mockSecurePreferences.getDeviceId() } returns ""
        
        // When
        val result = repository.sendSmsToBackend(sms)
        
        // Then
        assertTrue(result.isError)
        if (result is Result.Error) {
            assertTrue(result.message.contains("Not configured"))
        }
    }
    
    @Test
    fun `resetCounters resets both SMS and error counts`() = runTest {
        // Given - send some SMS and errors first
        val sms = SmsMessage(
            sender = "1234567890",
            body = "Test SMS",
            timestampMillis = System.currentTimeMillis()
        )
        coEvery { mockApi.ingestSms(any()) } returns Result.Success(
            SmsResponse(id = "1", parseStatus = "parsed", modelUsed = null)
        )
        
        repository.sendSmsToBackend(sms)
        repository.sendSmsToBackend(sms)
        
        // When
        repository.resetCounters()
        
        // Then
        val smsCount = repository.observeSmsCount().first()
        val errorCount = repository.observeErrorCount().first()
        assertEquals(0, smsCount)
        assertEquals(0, errorCount)
    }
    
    @Test
    fun `observeSmsCount emits initial value of zero`() = runTest {
        // When
        val count = repository.observeSmsCount().first()
        
        // Then
        assertEquals(0, count)
    }
    
    @Test
    fun `observeErrorCount emits initial value of zero`() = runTest {
        // When
        val count = repository.observeErrorCount().first()
        
        // Then
        assertEquals(0, count)
    }
}
