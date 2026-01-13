package com.ikanisa.smsgateway.util

/**
 * Represents the capability of the device to perform biometric authentication.
 */
enum class BiometricCapability {
    /** Biometric authentication is available and can be used. */
    AVAILABLE,
    
    /** The device does not have biometric hardware. */
    NO_HARDWARE,
    
    /** Biometric hardware is temporarily unavailable. */
    HARDWARE_UNAVAILABLE,
    
    /** No biometric credentials are enrolled (fingerprints/faces). */
    NOT_ENROLLED,
    
    /** Unknown error occurred while checking capability. */
    UNKNOWN_ERROR
}
