package com.ikanisa.smsgateway.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ikanisa.smsgateway.data.security.SecurePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for biometric authentication.
 * 
 * Handles fingerprint and face authentication using Android's BiometricPrompt API.
 */
@Singleton
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securePreferences: SecurePreferences
) {
    private val biometricManager = BiometricManager.from(context)
    
    /**
     * Check if biometric authentication is available on the device.
     */
    fun canAuthenticate(): BiometricCapability {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricCapability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricCapability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricCapability.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricCapability.NOT_ENROLLED
            else -> BiometricCapability.UNKNOWN_ERROR
        }
    }
    
    /**
     * Check if biometric authentication is enabled by the user.
     */
    fun isBiometricEnabled(): Boolean {
        return securePreferences.isBiometricEnabled()
    }
    
    /**
     * Enable or disable biometric authentication setting.
     */
    fun setBiometricEnabled(enabled: Boolean) {
        securePreferences.setBiometricEnabled(enabled)
    }
    
    /**
     * Authenticate the user with biometric.
     * 
     * @param activity The FragmentActivity to show the prompt on
     * @param title Title text for the prompt
     * @param subtitle Subtitle text for the prompt
     * @param onSuccess Callback when authentication succeeds
     * @param onError Callback when authentication encounters an error
     * @param onFailed Callback when authentication fails (e.g., unrecognized fingerprint)
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Verify your identity",
        subtitle: String = "Use your fingerprint or face to continue",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errorMessage: String) -> Unit,
        onFailed: () -> Unit
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancel")
            .setConfirmationRequired(true)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        
        val biometricPrompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errorCode, errString.toString())
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }
        )
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    companion object {
        /** Error code when user cancels biometric authentication */
        const val ERROR_USER_CANCELED = BiometricPrompt.ERROR_USER_CANCELED
        
        /** Error code when biometric is locked out temporarily */
        const val ERROR_LOCKOUT = BiometricPrompt.ERROR_LOCKOUT
        
        /** Error code when biometric is locked out permanently */
        const val ERROR_LOCKOUT_PERMANENT = BiometricPrompt.ERROR_LOCKOUT_PERMANENT
    }
}
