package com.ikanisa.smsgateway.domain.model

/**
 * Contains sync statistics for monitoring sync progress.
 */
data class SyncStats(
    /** Count of messages pending sync */
    val pending: Int,
    
    /** Count of messages currently syncing */
    val syncing: Int,
    
    /** Count of successfully synced messages */
    val synced: Int,
    
    /** Count of messages that failed to sync */
    val failed: Int
) {
    /** Total number of messages tracked */
    val total: Int get() = pending + syncing + synced + failed
    
    /** Percentage of messages synced (0-100) */
    val syncedPercentage: Int get() = if (total > 0) (synced * 100 / total) else 0
}
