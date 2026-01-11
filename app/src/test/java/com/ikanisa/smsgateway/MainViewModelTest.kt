package com.ikanisa.smsgateway

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ikanisa.smsgateway.data.repository.SmsRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for MainViewModel.
 * Tests log management, counters, and listening state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    @MockK
    private lateinit var repository: SmsRepository
    
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: MainViewModel
    
    private val smsCountFlow = MutableStateFlow(0)
    private val errorCountFlow = MutableStateFlow(0)
    
    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        Dispatchers.setMain(testDispatcher)
        
        // Setup StateFlow mocks
        every { repository.observeSmsCount() } returns smsCountFlow
        every { repository.observeErrorCount() } returns errorCountFlow
        
        viewModel = MainViewModel(repository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    // =========================================================================
    // Log Management Tests
    // =========================================================================
    
    @Test
    fun `appendLog adds timestamp and message to logs`() {
        // When
        viewModel.appendLog("Test message")
        
        // Then
        val logs = viewModel.logs.value
        assertNotNull(logs)
        assertTrue(logs!!.contains("Test message"))
        // Check timestamp format (HH:mm:ss)
        assertTrue(logs.matches(Regex(".*\\d{2}:\\d{2}:\\d{2}.*")))
    }
    
    @Test
    fun `appendLog appends multiple messages in order`() {
        // When
        viewModel.appendLog("First message")
        viewModel.appendLog("Second message")
        viewModel.appendLog("Third message")
        
        // Then
        val logs = viewModel.logs.value!!
        val firstIndex = logs.indexOf("First message")
        val secondIndex = logs.indexOf("Second message")
        val thirdIndex = logs.indexOf("Third message")
        
        assertTrue(firstIndex < secondIndex)
        assertTrue(secondIndex < thirdIndex)
    }
    
    @Test
    fun `clearLogs removes all log entries`() {
        // Given
        viewModel.appendLog("Message 1")
        viewModel.appendLog("Message 2")
        
        // When
        viewModel.clearLogs()
        
        // Then
        val logs = viewModel.logs.value
        assertTrue(logs.isNullOrEmpty())
    }
    
    // =========================================================================
    // Listening State Tests
    // =========================================================================
    
    @Test
    fun `setListening updates state to true`() {
        // When
        viewModel.setListening(true)
        
        // Then
        assertEquals(true, viewModel.isListening.value)
    }
    
    @Test
    fun `setListening updates state to false`() {
        // Given
        viewModel.setListening(true)
        
        // When
        viewModel.setListening(false)
        
        // Then
        assertEquals(false, viewModel.isListening.value)
    }
    
    @Test
    fun `isListening toggles correctly`() {
        // Toggle to true
        viewModel.setListening(true)
        assertEquals(true, viewModel.isListening.value)
        
        // Toggle to false
        viewModel.setListening(false)
        assertEquals(false, viewModel.isListening.value)
    }
    
    // =========================================================================
    // State Flow Tests
    // =========================================================================
    
    @Test
    fun `smsCount starts at initial value`() {
        // Initial value should be 0
        assertEquals(0, viewModel.smsCount.value)
    }
    
    @Test
    fun `errorCount starts at initial value`() {
        // Initial value should be 0
        assertEquals(0, viewModel.errorCount.value)
    }
}
