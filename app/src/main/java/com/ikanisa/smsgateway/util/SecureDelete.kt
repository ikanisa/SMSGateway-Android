package com.ikanisa.smsgateway.util

import java.util.Arrays

/**
 * Utilities for securely wiping sensitive data from memory.
 * 
 * Important: These methods attempt to overwrite sensitive data,
 * but garbage collection may have already copied the data elsewhere.
 * For maximum security, use ephemeral byte arrays and wipe them immediately.
 */
object SecureDelete {
    
    /**
     * Securely wipe a String by filling its backing char array with null characters.
     * 
     * Note: Strings in JVM are immutable and may be interned, so this might not
     * fully remove the data from memory, but it clears the reference.
     * 
     * @param sensitive The string containing sensitive data to wipe
     */
    fun wipeString(sensitive: String?) {
        if (sensitive == null) return
        
        try {
            // Access the internal char array via reflection
            val valueField = String::class.java.getDeclaredField("value")
            valueField.isAccessible = true
            val chars = valueField.get(sensitive) as? CharArray
            chars?.let { Arrays.fill(it, '\u0000') }
        } catch (e: Exception) {
            // If reflection fails, we can't wipe directly
            // This is expected on some JVM implementations
        }
    }
    
    /**
     * Securely wipe a byte array by overwriting with zeros.
     * 
     * @param sensitive The byte array containing sensitive data to wipe
     */
    fun wipeByteArray(sensitive: ByteArray?) {
        sensitive?.let { Arrays.fill(it, 0.toByte()) }
    }
    
    /**
     * Securely wipe a char array by overwriting with null characters.
     * 
     * @param sensitive The char array containing sensitive data to wipe
     */
    fun wipeCharArray(sensitive: CharArray?) {
        sensitive?.let { Arrays.fill(it, '\u0000') }
    }
    
    /**
     * Create a wiped copy of a string for use in comparisons.
     * This returns the original string but schedules it for wiping after use.
     * 
     * Usage:
     * ```
     * val password = SecureDelete.useAndWipe(sensitivePassword) { pwd ->
     *     authenticate(pwd)
     * }
     * ```
     */
    inline fun <T> useAndWipe(sensitive: String?, block: (String?) -> T): T {
        return try {
            block(sensitive)
        } finally {
            wipeString(sensitive)
        }
    }
    
    /**
     * Create a wiped copy of a byte array for use.
     */
    inline fun <T> useAndWipe(sensitive: ByteArray?, block: (ByteArray?) -> T): T {
        return try {
            block(sensitive)
        } finally {
            wipeByteArray(sensitive)
        }
    }
}
