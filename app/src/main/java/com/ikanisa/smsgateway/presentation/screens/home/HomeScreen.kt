package com.ikanisa.smsgateway.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ikanisa.smsgateway.presentation.components.ConnectionStatus
import com.ikanisa.smsgateway.presentation.components.FluidStatusIndicator
import com.ikanisa.smsgateway.presentation.components.LiquidGlassCard
import com.ikanisa.smsgateway.presentation.theme.AppAnimations
import com.ikanisa.smsgateway.presentation.theme.AppGradients

// Extension for relative time
fun Long.toRelativeTimeString(): String {
    val diff = System.currentTimeMillis() - this
    return when {
         diff < 60000 -> "Just now"
         diff < 3600000 -> "${diff / 60000}m ago"
         else -> "${diff / 3600000}h ago"
    }
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val listState = rememberLazyListState()
    
    Scaffold(
        topBar = {
            HomeTopBar(
                connectionStatus = uiState.connectionStatus,
                onSettingsClick = onNavigateToSettings
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            
            uiState.error != null -> {
                 Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = uiState.error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
                }
            }
            
            uiState.messages.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No messages yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Stats Section
                    StatsSection(
                        stats = uiState.stats,
                        modifier = Modifier.padding(16.dp)
                    )
                    
                    // Messages List
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.messages,
                            key = { it.id }
                        ) { message ->
                            AnimatedVisibility(
                                visible = true,
                                enter = AppAnimations.slideInFromBottom() + AppAnimations.scaleIn(),
                                exit = AppAnimations.slideOutToBottom()
                            ) {
                                MessageCard(
                                    message = message,
                                    onClick = { viewModel.selectMessage(message) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    connectionStatus: ConnectionStatus,
    onSettingsClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SMS Gateway",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FluidStatusIndicator(
                    status = connectionStatus,
                    showLabel = true,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun StatsSection(
    stats: SmsStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Total",
            value = stats.total.toString(),
            gradient = AppGradients.PrimaryGradient,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Synced",
            value = stats.synced.toString(),
            gradient = AppGradients.SuccessGradient,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Pending",
            value = stats.pending.toString(),
            gradient = Brush.linearGradient(
                listOf(Color(0xFFF59E0B), Color(0xFFD97706))
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun MessageCard(
    message: SmsMessage,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    LiquidGlassCard(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.Click)
            onClick()
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                SyncStatusBadge(status = message.syncStatus)
            }
            
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = message.receivedAt.toRelativeTimeString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun SyncStatusBadge(status: SyncStatus) {
    val (color, text) = when (status) {
        SyncStatus.SYNCED -> Color(0xFF10B981) to "Synced"
        SyncStatus.SYNCING -> Color(0xFFF59E0B) to "Syncing"
        SyncStatus.PENDING -> Color(0xFF6B7280) to "Pending"
        SyncStatus.FAILED -> Color(0xFFEF4444) to "Failed"
    }
    
    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}
