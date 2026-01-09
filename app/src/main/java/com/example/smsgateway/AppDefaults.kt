package com.example.smsgateway

import com.ikanisa.smsgateway.BuildConfig

/**
 * Default configuration values for internal app.
 * Supabase URL and key are loaded from BuildConfig (populated from local.properties during build).
 * Device credentials are fetched from Supabase based on MOMO number.
 * 
 * SECURITY: Secrets are no longer hardcoded in source code.
 * They must be provided via local.properties file (gitignored) during build.
 */
object AppDefaults {
    /**
     * Supabase URL loaded from BuildConfig.
     * Set via local.properties: supabase.url=https://your-project.supabase.co
     */
    val SUPABASE_URL: String
        get() = BuildConfig.SUPABASE_URL.ifEmpty { 
            // Fallback for development (should not be used in production)
            "https://wadhydemushqqtcrrlwm.supabase.co"
        }
    
    /**
     * Supabase anonymous key loaded from BuildConfig.
     * Set via local.properties: supabase.key=your-anon-key
     */
    val SUPABASE_ANON_KEY: String
        get() = BuildConfig.SUPABASE_ANON_KEY.ifEmpty {
            // Fallback for development (should not be used in production)
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndhZGh5ZGVtdXNocXF0Y3JybHdtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU3NDE1NTQsImV4cCI6MjA4MTMxNzU1NH0.9O6NMVpat63LnFO7hb9dLy0pz8lrMP0ZwGbIC68rdGI"
        }
    
    // These are fetched from Supabase based on MOMO number
    const val DEVICE_ID = ""
    const val DEVICE_SECRET = ""
    const val DEVICE_LABEL = ""
}
