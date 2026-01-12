package com.ikanisa.smsgateway.data.repository

import com.ikanisa.smsgateway.data.Result

/**
 * Repository interface for device activation operations.
 * Manages MOMO code validation and device registration linking.
 */
interface ActivationRepository {
    
    /**
     * Validate MOMO code against admin-registered devices.
     * Returns true if the MOMO code exists in the system and is not yet activated.
     */
    suspend fun validateMomoCode(momoCode: String): Result<Boolean>
    
    /**
     * Activate device with the given MOMO code.
     * Links the device to the staff account and marks as activated.
     */
    suspend fun activateDevice(momoCode: String, deviceId: String): Result<Unit>
    
    /**
     * Check if the current device is already activated.
     */
    suspend fun isDeviceActivated(): Boolean
    
    /**
     * Get the stored MOMO code for this device.
     */
    fun getStoredMomoCode(): String?
    
    /**
     * Store activation state locally.
     */
    suspend fun storeActivationState(momoCode: String, isActivated: Boolean)
}
