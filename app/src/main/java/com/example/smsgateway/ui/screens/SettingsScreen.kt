package com.example.smsgateway.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.smsgateway.MainViewModel
import com.example.smsgateway.ui.components.GatewayCredentialsSheet
import com.example.smsgateway.ui.components.MoMoIdentitySheet
import com.example.smsgateway.ui.components.SettingsCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onSaveGateway: (url: String, key: String, id: String, secret: String, label: String) -> Unit,
    onSaveMoMo: (number: String, code: String) -> Unit,
    getGatewayState: () -> Pair<String, Boolean>,
    getMoMoState: () -> String,
    getGatewayCredentials: () -> Tuple5<String, String, String, String, String>,
    getMoMoCredentials: () -> Pair<String, String>,
    requestSmsPermissions: (callback: (Boolean) -> Unit) -> Unit,
    checkSmsPermissions: () -> Boolean
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val gatewaySheetState = rememberModalBottomSheetState()
    val momoSheetState = rememberModalBottomSheetState()

    var showGatewaySheet by remember { mutableStateOf(false) }
    var showMoMoSheet by remember { mutableStateOf(false) }

    val (gatewayStatus, isConfigured) = getGatewayState()
    val momoStatus = getMoMoState()
    val (url, key, id, secret, label) = getGatewayCredentials()
    val (momoNum, momoCode) = getMoMoCredentials()

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
            // Gateway Card
            SettingsCard(
                title = "Gateway",
                subtitle = gatewayStatus,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Credentials are stored on-device and not displayed here",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Button(
                    onClick = { showGatewaySheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Edit Credentials")
                }
            }

            // MoMo Identity Card
            SettingsCard(
                title = "MoMo Identity",
                subtitle = momoStatus,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Button(
                    onClick = { showMoMoSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text("Edit MoMo Identity")
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
                            color = if (hasSmsPerms) androidx.compose.ui.graphics.Color(0xFF4CAF50) else androidx.compose.ui.graphics.Color(0xFFFF9800)
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

    // Bottom Sheets
    GatewayCredentialsSheet(
        sheetState = gatewaySheetState,
        isVisible = showGatewaySheet,
        supabaseUrl = url,
        supabaseKey = key,
        deviceId = id,
        deviceSecret = secret,
        deviceLabel = label,
        onSave = { newUrl, newKey, newId, newSecret, newLabel ->
            onSaveGateway(newUrl, newKey, newId, newSecret, newLabel)
            scope.launch {
                snackbarHostState.showSnackbar("Credentials saved")
                showGatewaySheet = false
                gatewaySheetState.hide()
            }
        },
        onDismiss = {
            scope.launch {
                showGatewaySheet = false
                gatewaySheetState.hide()
            }
        }
    )

    MoMoIdentitySheet(
        sheetState = momoSheetState,
        isVisible = showMoMoSheet,
        momoNumber = momoNum,
        momoCode = momoCode,
        onSave = { newNum, newCode ->
            onSaveMoMo(newNum, newCode)
            scope.launch {
                snackbarHostState.showSnackbar("MoMo identity saved")
                showMoMoSheet = false
                momoSheetState.hide()
            }
        },
        onDismiss = {
            scope.launch {
                showMoMoSheet = false
                momoSheetState.hide()
            }
        }
    )
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

// Helper data class for tuple
data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E) {
    operator fun component1() = a
    operator fun component2() = b
    operator fun component3() = c
    operator fun component4() = d
    operator fun component5() = e
}
