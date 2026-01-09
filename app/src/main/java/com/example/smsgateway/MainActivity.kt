package com.example.smsgateway

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.example.smsgateway.ui.screens.HomeScreen
import com.example.smsgateway.ui.screens.SettingsScreen
import com.example.smsgateway.ui.theme.SMSGatewayTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    private var permissionCallback: ((Boolean) -> Unit)? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            viewModel.setListening(true)
            viewModel.appendLog("SMS permissions granted - App is listening")
        } else {
            viewModel.setListening(false)
            viewModel.appendLog("SMS permissions denied - App cannot receive SMS")
        }
        permissionCallback?.invoke(allGranted)
        permissionCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // No longer needed - Worker uses Repository pattern instead of static ViewModel

        // Check if device is provisioned
        val deviceLookupHelper = DeviceLookupHelper(this)
        if (deviceLookupHelper.isProvisioned()) {
            val deviceInfo = deviceLookupHelper.getStoredDeviceInfo()
            viewModel.appendLog("✅ Device provisioned: ${deviceInfo?.deviceLabel ?: "Unknown"}")
        } else {
            viewModel.appendLog("⚠️ Device not configured: Enter your MoMo number in Settings")
        }

        checkAndRequestPermissions()

        setContent {
            SMSGatewayTheme {
                val navController = rememberNavController()
                val logs by viewModel.logs.observeAsState("")
                // Use StateFlow from repository
                val smsCount by viewModel.smsCount.collectAsState()
                val errorCount by viewModel.errorCount.collectAsState()
                val isListening by viewModel.isListening.observeAsState(false)

                val isConfigured = remember(logs) {
                    isConfiguredCheck(this@MainActivity)
                }

                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            context = this@MainActivity,
                            logs = logs ?: "",
                            smsCount = smsCount ?: 0,
                            errorCount = errorCount ?: 0,
                            isListening = isListening ?: false,
                            isConfigured = isConfigured,
                            onNavigateToSettings = {
                                navController.navigate("settings")
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            requestSmsPermissions = { callback ->
                                requestSmsPerms(callback)
                            },
                            checkSmsPermissions = {
                                checkSmsPerms()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = mutableListOf<String>()

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.RECEIVE_SMS)
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_SMS)
            }

            if (permissions.isNotEmpty()) {
                requestPermissionLauncher.launch(permissions.toTypedArray())
            } else {
                viewModel.setListening(true)
                viewModel.appendLog("SMS permissions already granted - App is listening")
            }
        } else {
            viewModel.setListening(true)
            viewModel.appendLog("App is listening (Android < 6.0)")
        }
    }

    private fun requestSmsPerms(callback: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
            permissionCallback = callback
            requestPermissionLauncher.launch(permissions)
        } else {
            callback(true)
        }
    }

    private fun checkSmsPerms(): Boolean {
        val rcv = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        return rcv && read
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasPermissions = checkSmsPerms()
            viewModel.setListening(hasPermissions)
        }
    }

    private fun isConfiguredCheck(context: Context): Boolean {
        val prefs = com.example.smsgateway.data.SecurePreferences(context)
        val devId = prefs.getDeviceId()?.trim().orEmpty()
        val devSecret = prefs.getDeviceSecret()?.trim().orEmpty()
        return devId.isNotEmpty() && devSecret.isNotEmpty()
    }
}
