package com.example.smsgateway.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.example.smsgateway.MainViewModel
import com.example.smsgateway.ui.components.LogsCard
import com.example.smsgateway.ui.components.MetricCard
import com.example.smsgateway.ui.components.StatusCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    context: Context,
    logs: String,
    smsCount: Int,
    errorCount: Int,
    isListening: Boolean,
    isConfigured: Boolean,
    onNavigateToSettings: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SMSGateway",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
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
            StatusCard(
                isListening = isListening,
                isConfigured = isConfigured,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                MetricCard(
                    title = "Processed",
                    value = smsCount,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                MetricCard(
                    title = "Errors",
                    value = errorCount,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }

            LogsCard(
                logs = logs,
                onCopyLogs = {
                    val clipboard = context.getSystemService<ClipboardManager>()
                    val clip = android.content.ClipData.newPlainText("SMSGateway Logs", logs)
                    clipboard?.setPrimaryClip(clip)
                    scope.launch {
                        snackbarHostState.showSnackbar("Logs copied to clipboard")
                    }
                },
                onClearLogs = {
                    viewModel.clearLogs()
                    scope.launch {
                        snackbarHostState.showSnackbar("Logs cleared")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
