package com.ikanisa.smsgateway

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import com.ikanisa.smsgateway.ui.screens.HomeScreen
import com.ikanisa.smsgateway.ui.theme.SMSGatewayTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

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

        viewModel.appendLog("SMS Gateway ready")
        checkAndRequestPermissions()

        setContent {
            SMSGatewayTheme {
                val logs by viewModel.logs.observeAsState("")
                val smsCount by viewModel.smsCount.collectAsState()
                val errorCount by viewModel.errorCount.collectAsState()
                val isListening by viewModel.isListening.observeAsState(false)

                HomeScreen(
                    viewModel = viewModel,
                    context = this@MainActivity,
                    logs = logs ?: "",
                    smsCount = smsCount ?: 0,
                    errorCount = errorCount ?: 0,
                    isListening = isListening ?: false
                )
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

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasPermissions = ContextCompat.checkSelfPermission(
                this, 
                Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED
            viewModel.setListening(hasPermissions)
        }
    }
}
