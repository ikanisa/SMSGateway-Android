package com.ikanisa.smsgateway.ui.components

import com.ikanisa.smsgateway.ui.theme.BrandPrimary
import com.ikanisa.smsgateway.ui.theme.Error
import com.ikanisa.smsgateway.ui.theme.Success
import com.ikanisa.smsgateway.ui.theme.Warning
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for StatusType enum.
 */
class StatusTypeTest {
    
    @Test
    fun `StatusType has all expected values`() {
        // Verify all status types exist
        val statusTypes = StatusType.values()
        
        assertEquals(5, statusTypes.size)
        assertTrue(statusTypes.contains(StatusType.SUCCESS))
        assertTrue(statusTypes.contains(StatusType.WARNING))
        assertTrue(statusTypes.contains(StatusType.ERROR))
        assertTrue(statusTypes.contains(StatusType.INFO))
        assertTrue(statusTypes.contains(StatusType.NEUTRAL))
    }
    
    @Test
    fun `StatusType valueOf works correctly`() {
        assertEquals(StatusType.SUCCESS, StatusType.valueOf("SUCCESS"))
        assertEquals(StatusType.WARNING, StatusType.valueOf("WARNING"))
        assertEquals(StatusType.ERROR, StatusType.valueOf("ERROR"))
        assertEquals(StatusType.INFO, StatusType.valueOf("INFO"))
        assertEquals(StatusType.NEUTRAL, StatusType.valueOf("NEUTRAL"))
    }
    
    @Test
    fun `StatusType ordinal values are consistent`() {
        // Test ordinals for stable ordering
        assertEquals(0, StatusType.SUCCESS.ordinal)
        assertEquals(1, StatusType.WARNING.ordinal)
        assertEquals(2, StatusType.ERROR.ordinal)
        assertEquals(3, StatusType.INFO.ordinal)
        assertEquals(4, StatusType.NEUTRAL.ordinal)
    }
}
