package com.example.smsgateway

import com.example.smsgateway.data.repository.SmsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MainViewModelTest {
    
    private lateinit var viewModel: MainViewModel
    private lateinit var mockRepository: SmsRepository
    
    @Before
    fun setup() {
        mockRepository = mockk(relaxed = true)
        
        // Mock repository StateFlows
        coEvery { mockRepository.observeSmsCount() } returns MutableStateFlow(0)
        coEvery { mockRepository.observeErrorCount() } returns MutableStateFlow(0)
        
        viewModel = MainViewModel(mockRepository)
    }
    
    @Test
    fun `appendLog adds log entry with timestamp`() {
        // Given
        val message = "Test log message"
        
        // When
        viewModel.appendLog(message)
        
        // Then
        val logs = viewModel.logs.value
        assertTrue(logs.contains(message))
        assertTrue(logs.contains("[")) // Contains timestamp
    }
    
    @Test
    fun `clearLogs removes all log entries`() {
        // Given
        viewModel.appendLog("Log 1")
        viewModel.appendLog("Log 2")
        
        // When
        viewModel.clearLogs()
        
        // Then
        val logs = viewModel.logs.value
        assertEquals("", logs)
    }
    
    @Test
    fun `setListening updates listening state`() {
        // When
        viewModel.setListening(true)
        
        // Then
        assertEquals(true, viewModel.isListening.value)
        
        // When
        viewModel.setListening(false)
        
        // Then
        assertEquals(false, viewModel.isListening.value)
    }
    
    @Test
    fun `logs are limited to max entries`() {
        // Given
        val maxEntries = 100
        
        // When - add more than max entries
        repeat(maxEntries + 50) {
            viewModel.appendLog("Log $it")
        }
        
        // Then - should only keep last maxEntries
        val logs = viewModel.logs.value
        val logLines = logs.split("\n").filter { it.isNotEmpty() }
        assertTrue(logLines.size <= maxEntries)
    }
    
    @Test
    fun `resetCounters calls repository reset`() = runTest {
        // When
        viewModel.resetCounters()
        
        // Then
        coEvery { mockRepository.resetCounters() }
    }
}
