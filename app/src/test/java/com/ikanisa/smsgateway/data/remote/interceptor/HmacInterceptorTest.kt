package com.ikanisa.smsgateway.data.remote.interceptor

import com.ikanisa.smsgateway.data.security.SecurePreferences
import io.mockk.every
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for HmacInterceptor.
 */
class HmacInterceptorTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var securePreferences: SecurePreferences
    private lateinit var hmacInterceptor: HmacInterceptor
    private lateinit var okHttpClient: OkHttpClient
    
    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        securePreferences = mockk(relaxed = true)
        hmacInterceptor = HmacInterceptor(securePreferences)
        
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(hmacInterceptor)
            .build()
    }
    
    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
    
    @Test
    fun `request is passed through without signing when device not configured`() {
        every { securePreferences.getDeviceSecret() } returns null
        every { securePreferences.getDeviceId() } returns null
        
        mockWebServer.enqueue(MockResponse().setBody("OK"))
        
        val request = Request.Builder()
            .url(mockWebServer.url("/api/test"))
            .build()
        
        val response = okHttpClient.newCall(request).execute()
        
        assertEquals(200, response.code)
        
        val recordedRequest = mockWebServer.takeRequest()
        // No signature headers should be present
        assertEquals(null, recordedRequest.getHeader(HmacInterceptor.HEADER_SIGNATURE))
    }
    
    @Test
    fun `request includes signature headers when device is configured`() {
        every { securePreferences.getDeviceSecret() } returns "test-secret-key-12345"
        every { securePreferences.getDeviceId() } returns "device-123"
        
        mockWebServer.enqueue(MockResponse().setBody("OK"))
        
        val request = Request.Builder()
            .url(mockWebServer.url("/api/test"))
            .build()
        
        val response = okHttpClient.newCall(request).execute()
        
        assertEquals(200, response.code)
        
        val recordedRequest = mockWebServer.takeRequest()
        
        // Signature headers should be present
        assertNotNull(recordedRequest.getHeader(HmacInterceptor.HEADER_TIMESTAMP))
        assertNotNull(recordedRequest.getHeader(HmacInterceptor.HEADER_DEVICE_ID))
        assertNotNull(recordedRequest.getHeader(HmacInterceptor.HEADER_SIGNATURE))
        
        assertEquals("device-123", recordedRequest.getHeader(HmacInterceptor.HEADER_DEVICE_ID))
    }
    
    @Test
    fun `signature is valid hex string of correct length`() {
        every { securePreferences.getDeviceSecret() } returns "test-secret"
        every { securePreferences.getDeviceId() } returns "device-abc"
        
        mockWebServer.enqueue(MockResponse().setBody("OK"))
        
        val request = Request.Builder()
            .url(mockWebServer.url("/api/test"))
            .build()
        
        okHttpClient.newCall(request).execute()
        
        val recordedRequest = mockWebServer.takeRequest()
        val signature = recordedRequest.getHeader(HmacInterceptor.HEADER_SIGNATURE)
        
        assertNotNull(signature)
        // HMAC-SHA256 produces 64 hex characters (256 bits / 4 bits per hex char)
        assertEquals(64, signature?.length)
        
        // Verify it's a valid hex string
        assertTrue(signature?.matches(Regex("[0-9a-f]+")) == true)
    }
    
    @Test
    fun `timestamp header is numeric`() {
        every { securePreferences.getDeviceSecret() } returns "secret"
        every { securePreferences.getDeviceId() } returns "device"
        
        mockWebServer.enqueue(MockResponse().setBody("OK"))
        
        val request = Request.Builder()
            .url(mockWebServer.url("/api/test"))
            .build()
        
        okHttpClient.newCall(request).execute()
        
        val recordedRequest = mockWebServer.takeRequest()
        val timestamp = recordedRequest.getHeader(HmacInterceptor.HEADER_TIMESTAMP)
        
        assertNotNull(timestamp)
        // Should be a valid long number
        val timestampLong = timestamp?.toLongOrNull()
        assertNotNull(timestampLong)
        assertTrue(timestampLong!! > 0)
    }
}
