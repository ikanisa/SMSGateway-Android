package com.ikanisa.smsgateway.data.mapper

import com.ikanisa.smsgateway.data.local.entity.SmsMessageEntity
import com.ikanisa.smsgateway.data.model.SmsMessage
import com.ikanisa.smsgateway.domain.model.SyncStatus
import java.security.MessageDigest

/**
 * Extension functions for mapping between domain models and database entities.
 */

/**
 * Convert SmsMessageEntity to domain SmsMessage.
 */
fun SmsMessageEntity.toDomain(): SmsMessage {
    return SmsMessage(
        sender = sender,
        body = content,
        timestampMillis = receivedAt,
        simSlot = simSlot
    )
}

/**
 * Convert domain SmsMessage to SmsMessageEntity.
 */
fun SmsMessage.toEntity(): SmsMessageEntity {
    return SmsMessageEntity(
        sender = sender,
        content = body,
        receivedAt = timestampMillis,
        messageHash = calculateHash(),
        syncStatus = SyncStatus.PENDING,
        simSlot = simSlot
    )
}

/**
 * Calculate SHA-256 hash of message content for deduplication.
 * Uses sender + body + timestamp to create unique identifier.
 */
fun SmsMessage.calculateHash(): String {
    val input = "$sender|$body|$timestampMillis"
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(input.toByteArray())
    return hashBytes.joinToString("") { "%02x".format(it) }
}

/**
 * Convert list of entities to domain models.
 */
fun List<SmsMessageEntity>.toDomainList(): List<SmsMessage> {
    return map { it.toDomain() }
}
