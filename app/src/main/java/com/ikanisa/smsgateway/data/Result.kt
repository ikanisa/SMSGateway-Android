package com.ikanisa.smsgateway.data

import com.ikanisa.smsgateway.data.model.SmsResponse

/**
 * Sealed class representing the result of an operation.
 * Used for type-safe error handling throughout the app.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()
    
    val isSuccess: Boolean
        get() = this is Success
    
    val isError: Boolean
        get() = this is Error
}

/**
 * Type alias for SMS operation results.
 */
typealias SmsResult = Result<SmsResponse>
