package com.ikanisa.smsgateway.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity for storing security audit logs.
 * 
 * Tracks security-related events such as:
 * - Authentication attempts
 * - Security check failures
 * - Sensitive data access
 * - Configuration changes
 */
@Entity(
    tableName = "audit_logs",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["severity"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    /** Name/type of the security event. */
    val event: String,
    
    /** Severity level of the event. */
    val severity: String,
    
    /** Additional details about the event. */
    val details: String,
    
    /** Unix timestamp when the event occurred. */
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Severity levels for audit events.
 */
enum class AuditSeverity {
    /** Informational event, no action required. */
    INFO,
    
    /** Warning event, may require attention. */
    WARNING,
    
    /** Critical security event, requires immediate attention. */
    CRITICAL
}
