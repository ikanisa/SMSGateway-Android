package com.ikanisa.smsgateway.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ikanisa.smsgateway.data.local.entity.AuditLogEntity

/**
 * Data Access Object for audit logs.
 */
@Dao
interface AuditDao {
    
    /**
     * Insert a new audit log entry.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(auditLog: AuditLogEntity)
    
    /**
     * Insert multiple audit log entries.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(auditLogs: List<AuditLogEntity>)
    
    /**
     * Get recent audit logs, ordered by timestamp descending.
     * 
     * @param limit Maximum number of entries to return
     */
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 100): List<AuditLogEntity>
    
    /**
     * Get audit logs by severity level.
     */
    @Query("SELECT * FROM audit_logs WHERE severity = :severity ORDER BY timestamp DESC")
    suspend fun getBySeverity(severity: String): List<AuditLogEntity>
    
    /**
     * Get audit logs within a time range.
     */
    @Query("SELECT * FROM audit_logs WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getByTimeRange(startTime: Long, endTime: Long): List<AuditLogEntity>
    
    /**
     * Delete audit logs older than the specified timestamp.
     * 
     * @param olderThan Unix timestamp; logs before this will be deleted
     */
    @Query("DELETE FROM audit_logs WHERE timestamp < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long)
    
    /**
     * Delete all audit logs.
     */
    @Query("DELETE FROM audit_logs")
    suspend fun deleteAll()
    
    /**
     * Get total count of audit logs.
     */
    @Query("SELECT COUNT(*) FROM audit_logs")
    suspend fun getCount(): Int
}
