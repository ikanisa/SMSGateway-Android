package com.ikanisa.smsgateway.data.remote.interceptor

import com.ikanisa.smsgateway.data.security.SecurePreferences
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Interceptor that signs requests with HMAC-SHA256.
 * 
 * Adds the following headers to each request:
 * - X-Timestamp: Unix timestamp of the request
 * - X-Device-Id: Unique device identifier
 * - X-Signature: HMAC-SHA256 signature of the request
 * 
 * The signature is calculated over: method|url|body|timestamp
 */
@Singleton
class HmacInterceptor @Inject constructor(
    private val securePreferences: SecurePreferences
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        
        val deviceSecret = securePreferences.getDeviceSecret()
        val deviceId = securePreferences.getDeviceId()
        
        // Skip signing if device is not configured
        if (deviceSecret.isNullOrEmpty() || deviceId.isNullOrEmpty()) {
            Timber.w("Device not configured, skipping request signing")
            return chain.proceed(original)
        }
        
        // Generate timestamp
        val timestamp = System.currentTimeMillis().toString()
        
        // Get request body as string
        val bodyString = original.body?.let { body ->
            try {
                val buffer = okio.Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            } catch (e: Exception) {
                Timber.w(e, "Failed to read request body for signing")
                ""
            }
        } ?: ""
        
        // Build signature payload: method|url|body|timestamp
        val signaturePayload = buildSignaturePayload(
            method = original.method,
            url = original.url.toString(),
            body = bodyString,
            timestamp = timestamp
        )
        
        // Calculate HMAC-SHA256 signature
        val signature = calculateHmac(signaturePayload, deviceSecret)
        
        // Add security headers
        val signedRequest = original.newBuilder()
            .addHeader(HEADER_TIMESTAMP, timestamp)
            .addHeader(HEADER_DEVICE_ID, deviceId)
            .addHeader(HEADER_SIGNATURE, signature)
            .build()
        
        return chain.proceed(signedRequest)
    }
    
    /**
     * Build the payload string for signature calculation.
     */
    private fun buildSignaturePayload(
        method: String,
        url: String,
        body: String,
        timestamp: String
    ): String {
        return "$method|$url|$body|$timestamp"
    }
    
    /**
     * Calculate HMAC-SHA256 of the data using the provided key.
     */
    private fun calculateHmac(data: String, key: String): String {
        return try {
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), HMAC_ALGORITHM)
            mac.init(secretKey)
            val hmacBytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
            hmacBytes.joinToString("") { "%02x".format(it) }
        } catch (e: NoSuchAlgorithmException) {
            Timber.e(e, "HMAC algorithm not available")
            throw SecurityException("Failed to sign request: algorithm unavailable", e)
        } catch (e: InvalidKeyException) {
            Timber.e(e, "Invalid key for HMAC")
            throw SecurityException("Failed to sign request: invalid key", e)
        }
    }
    
    companion object {
        private const val HMAC_ALGORITHM = "HmacSHA256"
        
        const val HEADER_TIMESTAMP = "X-Timestamp"
        const val HEADER_DEVICE_ID = "X-Device-Id"
        const val HEADER_SIGNATURE = "X-Signature"
    }
}
