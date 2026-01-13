package com.ikanisa.smsgateway.domain.exception

/**
 * Exception thrown when API call fails.
 */
class ApiException(
    message: String,
    val statusCode: Int? = null
) : Exception(message)
