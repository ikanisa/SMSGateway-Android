package com.ikanisa.smsgateway.notification

import android.content.Context
import android.telephony.SmsManager
import com.ikanisa.smsgateway.data.Result
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SmsNotificationService.
 */
class SmsNotificationServiceTest {
    
    @MockK
    private lateinit var context: Context
    
    @MockK
    private lateinit var smsManager: SmsManager
    
    private lateinit var service: SmsNotificationService
    
    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        // Note: In actual test, would need to mock SmsManager.getDefault()
        // For now, testing the logic paths
    }
    
    // =========================================================================
    // Validation Tests
    // =========================================================================
    
    @Test
    fun `sendSms returns error for blank phone number`() = runTest {
        // Given
        service = SmsNotificationService(context)
        
        // When
        val result = service.sendSms("", "Hello")
        
        // Then
        assertTrue(result is Result.Error)
        assertEquals("Phone number and message cannot be empty", (result as Result.Error).message)
    }
    
    @Test
    fun `sendSms returns error for blank message`() = runTest {
        // Given
        service = SmsNotificationService(context)
        
        // When
        val result = service.sendSms("+1234567890", "")
        
        // Then
        assertTrue(result is Result.Error)
        assertEquals("Phone number and message cannot be empty", (result as Result.Error).message)
    }
    
    @Test
    fun `sendSms returns error for whitespace-only phone number`() = runTest {
        // Given
        service = SmsNotificationService(context)
        
        // When
        val result = service.sendSms("   ", "Hello")
        
        // Then
        assertTrue(result is Result.Error)
    }
    
    @Test
    fun `sendSms returns error for whitespace-only message`() = runTest {
        // Given
        service = SmsNotificationService(context)
        
        // When
        val result = service.sendSms("+1234567890", "   ")
        
        // Then
        assertTrue(result is Result.Error)
    }
    
    // =========================================================================
    // Bulk SMS Tests
    // =========================================================================
    
    @Test
    fun `sendBulkSms returns map with results for all numbers`() = runTest {
        // Given
        service = SmsNotificationService(context)
        val numbers = listOf("+111", "+222", "+333")
        
        // When
        val results = service.sendBulkSms(numbers, "")
        
        // Then
        assertEquals(3, results.size)
        assertTrue(results.containsKey("+111"))
        assertTrue(results.containsKey("+222"))
        assertTrue(results.containsKey("+333"))
    }
    
    @Test
    fun `sendBulkSms returns empty map for empty list`() = runTest {
        // Given
        service = SmsNotificationService(context)
        
        // When
        val results = service.sendBulkSms(emptyList(), "Hello")
        
        // Then
        assertTrue(results.isEmpty())
    }
}
