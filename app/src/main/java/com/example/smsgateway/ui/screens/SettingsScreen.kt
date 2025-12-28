package com.example.smsgateway.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smsgateway.DeviceLookupHelper
import com.example.smsgateway.MainViewModel
import com.example.smsgateway.ui.components.SettingsCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    requestSmsPermissions: (callback: (Boolean) -> Unit) -> Unit,
    checkSmsPermissions: () -> Boolean
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val deviceLookupHelper = remember { DeviceLookupHelper(context) }
    
    // State for MoMo input
    var momoNumber by remember { 
        mutableStateOf(
            context.getSharedPreferences("SMSGatewayPrefs", Context.MODE_PRIVATE)
                .getString("momo_msisdn", "") ?: ""
        ) 
    }
    var momoCode by remember { 
        mutableStateOf(
            context.getSharedPreferences("SMSGatewayPrefs", Context.MODE_PRIVATE)
                .getString("momo_code", "") ?: ""
        ) 
    }
    var isLoading by remember { mutableStateOf(false) }
    var isProvisioned by remember { mutableStateOf(deviceLookupHelper.isProvisioned()) }
    var deviceLabel by remember { 
        mutableStateOf(deviceLookupHelper.getStoredDeviceInfo()?.deviceLabel ?: "") 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(vertical = 16.dp)
        ) {
            // MoMo Identity Card - Main configuration
            SettingsCard(
                title = "MoMo Identity",
                subtitle = if (isProvisioned) "Registered ✅" else "Enter your MoMo number to activate",
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = momoNumber,
                        onValueChange = { momoNumber = it },
                        label = { Text("MoMo Phone Number") },
                        placeholder = { Text("e.g., 0788767816") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = momoCode,
                        onValueChange = { momoCode = it },
                        label = { Text("MoMo Code (optional)") },
                        placeholder = { Text("Your identifier") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (isProvisioned && deviceLabel.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Device: $deviceLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            if (momoNumber.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please enter your MoMo number")
                                }
                                return@Button
                            }
                            
                            isLoading = true
                            scope.launch {
                                val result = deviceLookupHelper.lookupAndSaveDevice(
                                    momoMsisdn = momoNumber.trim(),
                                    momoCode = momoCode.trim()
                                )
                                
                                isLoading = false
                                
                                when (result) {
                                    is DeviceLookupHelper.LookupResult.Success -> {
                                        isProvisioned = true
                                        deviceLabel = result.deviceInfo.deviceLabel
                                        viewModel.appendLog("✅ Device activated: ${result.deviceInfo.deviceLabel}")
                                        snackbarHostState.showSnackbar("Device activated successfully!")
                                    }
                                    is DeviceLookupHelper.LookupResult.Error -> {
                                        viewModel.appendLog("❌ Activation failed: ${result.message}")
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                }
                            }
                        },
                        enabled = !isLoading && momoNumber.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                        Text(if (isProvisioned) "Update" else "Activate Device")
                    }
                }
            }

            // Permissions Card
            SettingsCard(
                title = "Permissions & System",
                subtitle = "Manage app permissions",
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                val hasSmsPerms = checkSmsPermissions()

                // SMS Permissions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = "SMS Permissions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (hasSmsPerms) "Granted" else "Not granted",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hasSmsPerms) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                    }
                    Button(
                        onClick = {
                            requestSmsPermissions { granted ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (granted) "SMS permissions granted" else "SMS permissions not granted"
                                    )
                                }
                            }
                        }
                    ) {
                        Text("Manage")
                    }
                }

                // Notification Settings
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Configure notification settings",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            openNotificationSettings(context)
                        }
                    ) {
                        Text("Open")
                    }
                }

                // Battery Optimization
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Battery Optimization",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Keep processing in background",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            requestBatteryOptimization(context)
                        }
                    ) {
                        Text("Ignore")
                    }
                }
            }
        }
    }
}

private fun openNotificationSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}

private fun requestBatteryOptimization(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(settingsIntent)
        }
    }
}
