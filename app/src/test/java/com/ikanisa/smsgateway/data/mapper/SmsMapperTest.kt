package com.ikanisa.smsgateway.data.mapper

import com.ikanisa.smsgateway.data.local.entity.SmsMessageEntity
import com.ikanisa.smsgateway.data.model.SmsMessage
import com.ikanisa.smsgateway.domain.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests for SmsMapper extension functions.
 */
class SmsMapperTest {
    
    @Test
    fun `toDomain converts entity to domain model`() {
        // Given
        val entity = SmsMessageEntity(
            id = "test-id",
            sender = "+250780000000",
            content = "Test message content",
            receivedAt = 1704067200000L,
            messageHash = "abc123",
            syncStatus = SyncStatus.SYNCED,
            simSlot = 1
        )
        
        // When
        val domain = entity.toDomain()
        
        // Then
        assertEquals(entity.sender, domain.sender)
        assertEquals(entity.content, domain.body)
        assertEquals(entity.receivedAt, domain.timestampMillis)
        assertEquals(entity.simSlot, domain.simSlot)
    }
    
    @Test
    fun `toEntity converts domain model to entity`() {
        // Given
        val sms = SmsMessage(
            sender = "+250780000000",
            body = "Test message content",
            timestampMillis = 1704067200000L,
            simSlot = 0
        )
        
        // When
        val entity = sms.toEntity()
        
        // Then
        assertEquals(sms.sender, entity.sender)
        assertEquals(sms.body, entity.content)
        assertEquals(sms.timestampMillis, entity.receivedAt)
        assertEquals(sms.simSlot, entity.simSlot)
        assertEquals(SyncStatus.PENDING, entity.syncStatus)
    }
    
    @Test
    fun `calculateHash produces consistent hash for same input`() {
        // Given
        val sms1 = SmsMessage(
            sender = "+250780000000",
            body = "Test message",
            timestampMillis = 1704067200000L
        )
        val sms2 = SmsMessage(
            sender = "+250780000000",
            body = "Test message",
            timestampMillis = 1704067200000L
        )
        
        // When
        val hash1 = sms1.calculateHash()
        val hash2 = sms2.calculateHash()
        
        // Then
        assertEquals(hash1, hash2)
    }
    
    @Test
    fun `calculateHash produces different hash for different inputs`() {
        // Given
        val sms1 = SmsMessage(
            sender = "+250780000000",
            body = "Test message 1",
            timestampMillis = 1704067200000L
        )
        val sms2 = SmsMessage(
            sender = "+250780000000",
            body = "Test message 2",
            timestampMillis = 1704067200000L
        )
        
        // When
        val hash1 = sms1.calculateHash()
        val hash2 = sms2.calculateHash()
        
        // Then
        assertNotEquals(hash1, hash2)
    }
    
    @Test
    fun `toDomainList converts list of entities`() {
        // Given
        val entities = listOf(
            SmsMessageEntity(
                id = "1",
                sender = "+250780000001",
                content = "Message 1",
                receivedAt = 1704067200000L,
                messageHash = "hash1"
            ),
            SmsMessageEntity(
                id = "2",
                sender = "+250780000002",
                content = "Message 2",
                receivedAt = 1704067300000L,
                messageHash = "hash2"
            )
        )
        
        // When
        val domainList = entities.toDomainList()
        
        // Then
        assertEquals(2, domainList.size)
        assertEquals(entities[0].sender, domainList[0].sender)
        assertEquals(entities[1].sender, domainList[1].sender)
    }
}
