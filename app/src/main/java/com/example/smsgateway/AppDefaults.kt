package com.example.smsgateway

/**
 * Default configuration values for internal app.
 * Supabase URL and key are hardcoded for internal distribution.
 * Device credentials are fetched from Supabase based on MOMO number.
 */
object AppDefaults {
    // Hardcoded for internal app - managed by admin
    const val SUPABASE_URL = "https://wadhydemushqqtcrrlwm.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndhZGh5ZGVtdXNocXF0Y3JybHdtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU3NDE1NTQsImV4cCI6MjA4MTMxNzU1NH0.9O6NMVpat63LnFO7hb9dLy0pz8lrMP0ZwGbIC68rdGI"
    
    // These are fetched from Supabase based on MOMO number
    const val DEVICE_ID = ""
    const val DEVICE_SECRET = ""
    const val DEVICE_LABEL = ""
}
