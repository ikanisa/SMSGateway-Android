package com.ikanisa.smsgateway.data

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the Result sealed class.
 */
class ResultTest {
    
    // =========================================================================
    // Success Tests
    // =========================================================================
    
    @Test
    fun `Success contains data correctly`() {
        // Given
        val data = "test data"
        
        // When
        val result: Result<String> = Result.Success(data)
        
        // Then
        assertTrue(result is Result.Success)
        assertEquals(data, (result as Result.Success).data)
    }
    
    @Test
    fun `Success with complex object`() {
        // Given
        data class User(val id: String, val name: String)
        val user = User("123", "John")
        
        // When
        val result = Result.Success(user)
        
        // Then
        assertEquals("123", result.data.id)
        assertEquals("John", result.data.name)
    }
    
    @Test
    fun `Success with null data is valid`() {
        // Given/When
        val result = Result.Success<String?>(null)
        
        // Then
        assertTrue(result is Result.Success)
        assertNull(result.data)
    }
    
    // =========================================================================
    // Error Tests
    // =========================================================================
    
    @Test
    fun `Error contains message correctly`() {
        // Given
        val errorMessage = "Something went wrong"
        
        // When
        val result: Result<String> = Result.Error(errorMessage)
        
        // Then
        assertTrue(result is Result.Error)
        assertEquals(errorMessage, (result as Result.Error).message)
    }
    
    @Test
    fun `Error contains exception when provided`() {
        // Given
        val exception = RuntimeException("Network error")
        
        // When
        val result: Result<String> = Result.Error("Failed", exception)
        
        // Then
        assertTrue(result is Result.Error)
        assertEquals(exception, (result as Result.Error).exception)
    }
    
    @Test
    fun `Error without exception has null exception`() {
        // Given/When
        val result: Result<String> = Result.Error("Error message")
        
        // Then
        assertNull((result as Result.Error).exception)
    }
    
    // =========================================================================
    // Pattern Matching Tests
    // =========================================================================
    
    @Test
    fun `when expression works with Success`() {
        // Given
        val result: Result<Int> = Result.Success(42)
        
        // When
        val message = when (result) {
            is Result.Success -> "Got: ${result.data}"
            is Result.Error -> "Error: ${result.message}"
        }
        
        // Then
        assertEquals("Got: 42", message)
    }
    
    @Test
    fun `when expression works with Error`() {
        // Given
        val result: Result<Int> = Result.Error("Failed to fetch")
        
        // When
        val message = when (result) {
            is Result.Success -> "Got: ${result.data}"
            is Result.Error -> "Error: ${result.message}"
        }
        
        // Then
        assertEquals("Error: Failed to fetch", message)
    }
    
    // =========================================================================
    // Type Inference Tests
    // =========================================================================
    
    @Test
    fun `Result can hold different types`() {
        // String
        val stringResult: Result<String> = Result.Success("test")
        assertTrue(stringResult is Result.Success)
        
        // Int
        val intResult: Result<Int> = Result.Success(123)
        assertTrue(intResult is Result.Success)
        
        // List
        val listResult: Result<List<String>> = Result.Success(listOf("a", "b"))
        assertTrue(listResult is Result.Success)
        assertEquals(2, (listResult as Result.Success).data.size)
    }
}
