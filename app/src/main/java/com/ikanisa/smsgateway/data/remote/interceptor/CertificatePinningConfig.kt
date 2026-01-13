package com.ikanisa.smsgateway.data.remote.interceptor

import android.content.Context
import com.ikanisa.smsgateway.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Configuration for certificate pinning and secure OkHttpClient creation.
 * 
 * Note: Certificate hashes should be configured in local.properties for production.
 * Placeholder hashes are used until actual certificates are obtained.
 */
@Singleton
class CertificatePinningConfig @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Create a secured OkHttpClient with certificate pinning and TLS verification.
     */
    fun createSecureOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        
        // Add certificate pinning for production
        if (!BuildConfig.DEBUG) {
            val supabaseHost = extractHostname(BuildConfig.SUPABASE_URL)
            if (supabaseHost.isNotEmpty()) {
                val certificatePinner = CertificatePinner.Builder()
                    // Supabase uses Let's Encrypt certificates
                    // ISRG Root X1 (Let's Encrypt root - long-lived)
                    .add(supabaseHost, "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M=")
                    // Let's Encrypt R3 intermediate (current)
                    .add(supabaseHost, "sha256/jQJTbIh0grw0/1TkHSumWb+Fs0Ggogr621gT3PvPKG0=")
                    // Supabase current leaf certificate
                    .add(supabaseHost, "sha256/gq0i3k2tlOamMGsUv1/KzkNle4NlMUhIIJpbWNyzQgk=")
                    .build()
                
                builder.certificatePinner(certificatePinner)
            }
        }
        
        // Add TLS version verification
        builder.addNetworkInterceptor(TlsVersionInterceptor())
        
        // Add logging interceptor for debug builds only
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }
        
        return builder.build()
    }
    
    private fun extractHostname(url: String): String {
        return try {
            url.removePrefix("https://")
                .removePrefix("http://")
                .split("/")[0]
        } catch (e: Exception) {
            ""
        }
    }
    
    companion object {
        private const val TIMEOUT_SECONDS = 30L
    }
}

/**
 * Interceptor that verifies TLS 1.2+ is being used.
 */
class TlsVersionInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val connection = chain.connection()
        val tlsVersion = connection?.handshake()?.tlsVersion
        
        // Enforce TLS 1.2 or higher
        if (tlsVersion != null && 
            tlsVersion != TlsVersion.TLS_1_3 && 
            tlsVersion != TlsVersion.TLS_1_2) {
            throw SecurityException("Insecure TLS version detected: $tlsVersion. TLS 1.2+ required.")
        }
        
        return chain.proceed(chain.request())
    }
}
