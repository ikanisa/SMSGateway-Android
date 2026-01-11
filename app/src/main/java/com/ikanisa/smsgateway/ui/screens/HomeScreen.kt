package com.ikanisa.smsgateway.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.ikanisa.smsgateway.MainViewModel
import com.ikanisa.smsgateway.ui.components.LogsCard
import com.ikanisa.smsgateway.ui.components.MetricCard
import com.ikanisa.smsgateway.ui.components.PulsingIndicator
import com.ikanisa.smsgateway.ui.components.StatusCard
import com.ikanisa.smsgateway.ui.theme.BrandPrimary
import com.ikanisa.smsgateway.ui.theme.BrandSecondary
import com.ikanisa.smsgateway.ui.theme.GradientColors
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
    onNavigateToSettings: (() -> Unit)? = null
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Animated status dot in title
                        PulsingIndicator(
                            isActive = isListening,
                            size = 10
                        )
                        Column {
                            Text(
                                "SMS Gateway",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (isListening) "Active" else "Inactive",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (onNavigateToSettings != null) {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            // Decorative gradient circles
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.TopEnd)
                    .blur(100.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                BrandPrimary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.BottomStart)
                    .blur(100.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                BrandSecondary.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 8.dp)
            ) {
                // Status card with animation
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically { -it / 2 },
                    exit = fadeOut() + slideOutVertically { -it / 2 }
                ) {
                    StatusCard(
                        isListening = isListening,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Metrics row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Processed",
                        value = smsCount,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Errors",
                        value = errorCount,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Quick stats summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBadge(
                        value = "${if (smsCount > 0) ((smsCount.toFloat() / (smsCount + errorCount)) * 100).toInt() else 100}%",
                        label = "Success Rate"
                    )
                    StatBadge(
                        value = "${smsCount + errorCount}",
                        label = "Total"
                    )
                }

                // Logs card
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
                
                Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for FAB
            }
        }
    }
}

@Composable
private fun StatBadge(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BrandPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
