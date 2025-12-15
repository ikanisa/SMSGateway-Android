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
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smsgateway.ui.screens.HomeScreen
import com.example.smsgateway.ui.screens.SettingsScreen
import com.example.smsgateway.ui.screens.Tuple5
import com.example.smsgateway.ui.theme.SMSGatewayTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set ViewModel reference for Worker logs
        ProcessSmsWorker.setViewModel(viewModel)

        // Config sanity check
        val prefs = getSharedPreferences("SMSGatewayPrefs", Context.MODE_PRIVATE)
        val supaUrl = prefs.getString("supabase_url", "").orEmpty().trim()
        val supaKey = prefs.getString("supabase_key", "").orEmpty().trim()
        val devId = prefs.getString("device_id", "").orEmpty().trim()
        val devSecret = prefs.getString("device_secret", "").orEmpty().trim()
        if (supaUrl.isEmpty() || supaKey.isEmpty() || devId.isEmpty() || devSecret.isEmpty()) {
            viewModel.appendLog("⚠️ Not configured: open Settings and enter Supabase URL + Anon Key + Device ID + Device Secret")
        } else {
            viewModel.appendLog("✅ Provisioned: ready to forward SMS to Edge Function")
        }

        checkAndRequestPermissions()

        setContent {
            SMSGatewayTheme {
                val navController = rememberNavController()
                val logs by viewModel.logs.observeAsState("")
                val smsCount by viewModel.smsCount.observeAsState(0)
                val errorCount by viewModel.errorCount.observeAsState(0)
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
                            onSaveGateway = { url, key, id, secret, label ->
                                saveGatewayCredentials(url, key, id, secret, label)
                            },
                            onSaveMoMo = { number, code ->
                                saveMoMoCredentials(number, code)
                            },
                            getGatewayState = {
                                val prefs = getSharedPreferences("SMSGatewayPrefs", Context.MODE_PRIVATE)
                                val supaUrl = prefs.getString("supabase_url", "").orEmpty().trim()
                                val supaKey = prefs.getString("supabase_key", "").orEmpty().trim()
                                val devId = prefs.getString("device_id", "").orEmpty().trim()
                                val devSecret = prefs.getString("device_secret", "").orEmpty().trim()
                                val isConf = supaUrl.isNotEmpty() && supaKey.isNotEmpty() && devId.isNotEmpty() && devSecret.isNotEmpty()
                                val status = if (isConf) "Configured ✅" else "Not configured ⚠️"
                                Pair(status, isConf)
                            },
                            getMoMoState = {
                                val prefs = getSharedPreferences("SMSGatewayPrefs", Context.MODE_PRIVATE)
                                val momo = prefs.getString("momo_msisdn", "").orEmpty().trim()
                                val momoCode = prefs.getString("momo_code", "").orEmpty().trim()
                                when {
                                    momo.isNotEmpty() && momoCode.isNotEmpty() -> "MoMo: $momo (code: $momoCode)"
                                    momo.isNotEmpty() -> "MoMo: $momo"
                                    momoCode.isNotEmpty() -> "MoMo code: $momoCode"
                                    else -> "Not set"
                                }
                            },
                            getGatewayCredentials = {
                                val prefs = getSharedPreferences("SMSGatewayPrefs", Context.MODE_PRIVATE)
                                Tuple5(
                                    prefs.getString("supabase_url", "") ?: "",
                                    prefs.getString("supabase_key", "") ?: "",
                                    prefs.getString("device_id", "") ?: "",
                                    prefs.getString("device_secret", "") ?: "",
                                    prefs.getString("device_label", "") ?: ""
                                )
                            },
                            getMoMoCredentials = {
                                val prefs = getSharedPreferences("SMSGatewayPrefs", Context.MODE_PRIVATE)
                                Pair(
                                    prefs.getString("momo_msisdn", "") ?: "",
                                    prefs.getString("momo_code", "") ?: ""
                                )
                            },
                            requestSmsPermissions = { callback ->
                                requestSmsPerms { callback(it) }
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
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                callback(result.all { it.value })
            }.launch(permissions)
        } else {
            callback(true)
        }
    }

    private fun checkSmsPerms(): Boolean {
        val rcv = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        return rcv && read
    }

    private fun saveGatewayCredentials(url: String, key: String, id: String, secret: String, label: String) {
        val prefs = getSharedPreferences("SMSGatewayPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("supabase_url", url)
            .putString("supabase_key", key)
            .putString("device_id", id)
            .putString("device_secret", secret)
            .putString("device_label", label)
            .apply()
    }

    private fun saveMoMoCredentials(number: String, code: String) {
        val prefs = getSharedPreferences("SMSGatewayPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("momo_msisdn", number)
            .putString("momo_code", code)
            .apply()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasPermissions = checkSmsPerms()
            viewModel.setListening(hasPermissions)
        }
    }

    private fun isConfiguredCheck(context: Context): Boolean {
        val prefs = context.getSharedPreferences("SMSGatewayPrefs", Context.MODE_PRIVATE)
        val supaUrl = prefs.getString("supabase_url", "").orEmpty().trim()
        val supaKey = prefs.getString("supabase_key", "").orEmpty().trim()
        val devId = prefs.getString("device_id", "").orEmpty().trim()
        val devSecret = prefs.getString("device_secret", "").orEmpty().trim()
        return supaUrl.isNotEmpty() && supaKey.isNotEmpty() && devId.isNotEmpty() && devSecret.isNotEmpty()
    }
}
