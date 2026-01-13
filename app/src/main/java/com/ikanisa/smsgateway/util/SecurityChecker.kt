package com.ikanisa.smsgateway.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.ikanisa.smsgateway.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Security checks for device integrity and app tamper detection.
 * 
 * Performs comprehensive checks for:
 * - Root access
 * - Debugging status
 * - Hooking frameworks (Xposed, Substrate)
 * - Emulator detection
 * - Signature validation
 */
@Singleton
class SecurityChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Perform all security checks and return a result.
     * 
     * Note: In debug builds, some checks (emulator, debuggable) are skipped
     * to allow development.
     */
    fun performSecurityChecks(): SecurityCheckResult {
        val issues = mutableListOf<SecurityIssue>()
        
        if (isRooted()) {
            issues.add(SecurityIssue.ROOTED_DEVICE)
        }
        
        // Only check in release builds
        if (!BuildConfig.DEBUG) {
            if (isDebuggable()) {
                issues.add(SecurityIssue.DEBUGGABLE)
            }
            
            if (isEmulator()) {
                issues.add(SecurityIssue.EMULATOR)
            }
        }
        
        if (hasHookingFrameworks()) {
            issues.add(SecurityIssue.HOOKING_DETECTED)
        }
        
        if (!isSignatureValid()) {
            issues.add(SecurityIssue.TAMPERED_SIGNATURE)
        }
        
        return SecurityCheckResult(
            isSecure = issues.isEmpty(),
            issues = issues
        )
    }
    
    /**
     * Check for common root indicators.
     */
    fun isRooted(): Boolean {
        val rootPaths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/magisk/.core"
        )
        
        // Check for root binaries
        val hasRootBinaries = rootPaths.any { File(it).exists() }
        
        // Check for su binary execution
        val canExecuteSu = try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val result = process.inputStream.bufferedReader().readLine()
            process.waitFor()
            result != null
        } catch (e: Exception) {
            false
        }
        
        // Check for root management apps
        val rootApps = listOf(
            "com.topjohnwu.magisk",
            "com.koushikdutta.superuser",
            "com.noshufou.android.su",
            "com.thirdparty.superuser",
            "eu.chainfire.supersu"
        )
        
        val hasRootApps = rootApps.any { packageName ->
            try {
                context.packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
        
        return hasRootBinaries || canExecuteSu || hasRootApps
    }
    
    /**
     * Check if the app is in debuggable state.
     */
    fun isDebuggable(): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
    
    /**
     * Check for known hooking frameworks.
     */
    fun hasHookingFrameworks(): Boolean {
        val hookingClasses = listOf(
            "de.robv.android.xposed.XposedBridge",
            "de.robv.android.xposed.XposedHelpers",
            "com.saurik.substrate.MS",
            "com.saurik.substrate.MS\$2"
        )
        
        return hookingClasses.any { className ->
            try {
                Class.forName(className)
                Timber.w("Hooking framework detected: $className")
                true
            } catch (e: ClassNotFoundException) {
                false
            }
        }
    }
    
    /**
     * Check if running on an emulator.
     */
    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
                || Build.BRAND.startsWith("generic")
                || Build.DEVICE.startsWith("generic"))
    }
    
    /**
     * Validate app signature against known release signature.
     * 
     * Note: For Firebase App Distribution (internal deployment), signature
     * validation is relaxed. Returns true if no hash is configured.
     */
    @Suppress("DEPRECATION")
    fun isSignatureValid(): Boolean {
        // Skip signature check in debug builds
        if (BuildConfig.DEBUG) {
            return true
        }
        
        // Skip if no release hash is configured (common for internal distribution)
        if (BuildConfig.RELEASE_SIGNATURE_HASH.isEmpty() || 
            BuildConfig.RELEASE_SIGNATURE_HASH == "DEBUG") {
            Timber.d("Signature validation skipped - no hash configured (Firebase Distribution)")
            return true
        }
        
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }
            
            val currentSignature = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners?.firstOrNull()
            } else {
                packageInfo.signatures?.firstOrNull()
            }
            
            if (currentSignature == null) {
                Timber.w("No signature found")
                return false
            }
            
            val currentHash = currentSignature.toCharsString()
            val expectedHash = BuildConfig.RELEASE_SIGNATURE_HASH
            
            currentHash == expectedHash
        } catch (e: Exception) {
            Timber.e(e, "Failed to verify signature")
            false
        }
    }
}

/**
 * Result of security checks.
 */
data class SecurityCheckResult(
    /** Whether all security checks passed. */
    val isSecure: Boolean,
    /** List of security issues detected. */
    val issues: List<SecurityIssue>
)

/**
 * Types of security issues that can be detected.
 */
enum class SecurityIssue {
    /** Device has root access. */
    ROOTED_DEVICE,
    
    /** App is in debuggable mode. */
    DEBUGGABLE,
    
    /** Hooking framework detected (Xposed, Substrate, etc.). */
    HOOKING_DETECTED,
    
    /** Running on an emulator. */
    EMULATOR,
    
    /** App signature doesn't match expected release signature. */
    TAMPERED_SIGNATURE
}
