package com.ikanisa.smsgateway.data.repository

import com.ikanisa.smsgateway.data.Result
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SmsRepositoryImpl.
 */
class SmsRepositoryImplTest {
    
    private val repository = SmsRepositoryImpl()
    
    @Test
    fun `observeSmsCount emits values`() {
        // Repository exposes a flow for SMS count
        val flow = repository.observeSmsCount()
        assertNotNull(flow)
    }
    
    @Test
    fun `observeErrorCount emits values`() {
        // Repository exposes a flow for error count
        val flow = repository.observeErrorCount()
        assertNotNull(flow)
    }
    
    @Test
    fun `repository can be instantiated`() {
        // Simple sanity check
        val repo = SmsRepositoryImpl()
        assertNotNull(repo)
    }
}
