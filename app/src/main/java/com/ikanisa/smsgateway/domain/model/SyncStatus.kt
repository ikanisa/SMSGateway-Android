package com.ikanisa.smsgateway.domain.model

/**
 * Represents the synchronization status of an SMS message.
 */
enum class SyncStatus {
    /** Message saved locally, awaiting sync */
    PENDING,
    
    /** Message is currently being synced to backend */
    SYNCING,
    
    /** Message successfully synced to backend */
    SYNCED,
    
    /** Sync failed after maximum retries */
    FAILED
}
