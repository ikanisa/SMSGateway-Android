package com.ikanisa.smsgateway.util

import com.ikanisa.smsgateway.data.local.dao.AuditDao
import com.ikanisa.smsgateway.data.local.entity.AuditLogEntity
import com.ikanisa.smsgateway.data.local.entity.AuditSeverity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for logging security audit events.
 * 
 * Provides async logging to avoid blocking the main thread.
 * Events are stored in the local Room database for later analysis.
 */
@Singleton
class AuditLogger @Inject constructor(
    private val auditDao: AuditDao
) {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val scope = CoroutineScope(ioDispatcher)
    
    /**
     * Log a security event asynchronously.
     */
    fun log(event: SecurityEvent) {
        scope.launch {
            try {
                auditDao.insert(
                    AuditLogEntity(
                        event = event.name,
                        severity = event.severity.name,
                        details = event.details
                    )
                )
                
                // Also log to Timber for debugging
                when (event.severity) {
                    AuditSeverity.CRITICAL -> Timber.e("AUDIT [${event.name}]: ${event.details}")
                    AuditSeverity.WARNING -> Timber.w("AUDIT [${event.name}]: ${event.details}")
                    AuditSeverity.INFO -> Timber.i("AUDIT [${event.name}]: ${event.details}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to log audit event: ${event.name}")
            }
        }
    }
    
    /**
     * Log an info-level event.
     */
    fun logInfo(name: String, details: String) {
        log(SecurityEvent(name, AuditSeverity.INFO, details))
    }
    
    /**
     * Log a warning-level event.
     */
    fun logWarning(name: String, details: String) {
        log(SecurityEvent(name, AuditSeverity.WARNING, details))
    }
    
    /**
     * Log a critical-level event.
     */
    fun logCritical(name: String, details: String) {
        log(SecurityEvent(name, AuditSeverity.CRITICAL, details))
    }
    
    /**
     * Get recent audit events.
     */
    suspend fun getRecentEvents(limit: Int = 100): List<AuditLogEntity> {
        return try {
            auditDao.getRecent(limit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve audit events")
            emptyList()
        }
    }
    
    /**
     * Clean up old audit logs.
     * 
     * @param daysToKeep Number of days of logs to retain
     */
    suspend fun cleanup(daysToKeep: Int = 30) {
        try {
            val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
            auditDao.deleteOlderThan(cutoffTime)
            Timber.d("Cleaned up audit logs older than $daysToKeep days")
        } catch (e: Exception) {
            Timber.e(e, "Failed to cleanup audit logs")
        }
    }
    
    companion object {
        // Common event names
        const val EVENT_APP_START = "APP_START"
        const val EVENT_BIOMETRIC_AUTH = "BIOMETRIC_AUTH"
        const val EVENT_BIOMETRIC_FAILED = "BIOMETRIC_FAILED"
        const val EVENT_SECURITY_CHECK = "SECURITY_CHECK"
        const val EVENT_ROOT_DETECTED = "ROOT_DETECTED"
        const val EVENT_TAMPER_DETECTED = "TAMPER_DETECTED"
        const val EVENT_SMS_RECEIVED = "SMS_RECEIVED"
        const val EVENT_SMS_SYNCED = "SMS_SYNCED"
        const val EVENT_CONFIG_CHANGED = "CONFIG_CHANGED"
        const val EVENT_DEVICE_REGISTERED = "DEVICE_REGISTERED"
    }
}

/**
 * Represents a security event to be logged.
 */
data class SecurityEvent(
    val name: String,
    val severity: AuditSeverity,
    val details: String
)
