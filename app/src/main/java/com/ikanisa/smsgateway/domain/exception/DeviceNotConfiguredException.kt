package com.ikanisa.smsgateway.domain.exception

/**
 * Exception thrown when device is not configured/activated.
 */
class DeviceNotConfiguredException(
    message: String = "Device is not configured. Please activate with a MOMO code."
) : Exception(message)
