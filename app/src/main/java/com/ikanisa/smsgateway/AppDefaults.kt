package com.ikanisa.smsgateway

/**
 * Hardcoded configuration for SMS Gateway.
 * Simple, minimal configuration for appliance mode.
 */
object AppDefaults {
    const val SUPABASE_URL = "https://wadhydemushqqtcrrlwm.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndhZGh5ZGVtdXNocXF0Y3JybHdtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU3NDE1NTQsImV4cCI6MjA4MTMxNzU1NH0.9O6NMVpat63LnFO7hb9dLy0pz8lrMP0ZwGbIC68rdGI"
    
    /**
     * MoMo code - device identifier.
     * Set via BuildConfig or hardcode per device.
     */
    val MOMO_CODE: String
        get() = com.ikanisa.smsgateway.BuildConfig.MOMO_CODE.ifEmpty {
            "" // Will be set per device build
        }
    
    /**
     * Telco sender allowlist - only accept SMS from these senders.
     * Update when MTN changes sender IDs.
     */
    val TELCO_SENDER_ALLOWLIST = setOf(
        "MTN",
        "MTN MoMo",
        "MOMO",
        "MTNMobileMoney",
        "MTN Mobile Money",
        "100",
        "456",
        "MTN-100",
        "MTN-456"
    )
    
    /**
     * MTN MoMo SMS body patterns (regex).
     * Only forward SMS that match these patterns.
     * Common patterns:
     * - Transaction notifications: "You have received..." / "You sent..."
     * - Balance queries: "Your balance is..."
     * - Payment confirmations: "Payment of..." / "Paid to..."
     */
    val MTN_MOMO_BODY_PATTERNS = listOf(
        // Transaction received
        Regex(".*(?:received|credit|deposit).*\\d+.*(?:UGX|USD|RWF|KES|TZS).*", RegexOption.IGNORE_CASE),
        // Transaction sent
        Regex(".*(?:sent|paid|transfer|withdraw).*\\d+.*(?:UGX|USD|RWF|KES|TZS).*", RegexOption.IGNORE_CASE),
        // Balance query
        Regex(".*(?:balance|bal).*\\d+.*(?:UGX|USD|RWF|KES|TZS).*", RegexOption.IGNORE_CASE),
        // Payment confirmation
        Regex(".*(?:payment|paid|transaction).*(?:successful|completed|confirmed).*", RegexOption.IGNORE_CASE),
        // Generic MoMo pattern (amount + currency)
        Regex(".*\\d+.*(?:UGX|USD|RWF|KES|TZS).*", RegexOption.IGNORE_CASE)
    )
    
    /**
     * Check if sender is in allowlist.
     */
    fun isAllowedSender(sender: String): Boolean {
        val normalized = sender.trim()
        return TELCO_SENDER_ALLOWLIST.any { allowed ->
            normalized.equals(allowed, ignoreCase = true) ||
            normalized.contains(allowed, ignoreCase = true)
        }
    }
    
    /**
     * Check if body matches MTN MoMo patterns.
     */
    fun matchesMomoPattern(body: String): Boolean {
        val normalized = body.trim()
        if (normalized.isEmpty()) return false
        return MTN_MOMO_BODY_PATTERNS.any { pattern ->
            pattern.matches(normalized)
        }
    }
}
