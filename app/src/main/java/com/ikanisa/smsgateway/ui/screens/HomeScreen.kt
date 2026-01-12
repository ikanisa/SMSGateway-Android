package com.ikanisa.smsgateway.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.ikanisa.smsgateway.AppDefaults
import com.ikanisa.smsgateway.MainViewModel
import com.ikanisa.smsgateway.ui.components.ConnectionPulse
import com.ikanisa.smsgateway.ui.components.GlassCard
import com.ikanisa.smsgateway.ui.components.PulseStatus
import com.ikanisa.smsgateway.ui.components.QuickActionCard
import com.ikanisa.smsgateway.ui.components.StatusPulse
import com.ikanisa.smsgateway.ui.theme.BrandAccent
import com.ikanisa.smsgateway.ui.theme.BrandPrimary
import com.ikanisa.smsgateway.ui.theme.BrandSecondary
import com.ikanisa.smsgateway.ui.theme.Error
import com.ikanisa.smsgateway.ui.theme.GradientColors
import com.ikanisa.smsgateway.ui.theme.Success
import com.ikanisa.smsgateway.ui.theme.ThemeColors
import com.ikanisa.smsgateway.ui.theme.Warning
import kotlinx.coroutines.launch

/**
 * Revamped Home Screen matching the React component design.
 * Features hero status card, quick actions, and recent activity list.
 */
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    context: Context,
    logs: String,
    smsCount: Int,
    errorCount: Int,
    isListening: Boolean,
    isDarkTheme: Boolean = true,
    onNavigateToSettings: (() -> Unit)? = null
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val momoCode = AppDefaults.MOMO_CODE.ifEmpty { "Not Set" }
    
    // Calculate stats
    val successRate = if (smsCount + errorCount > 0) {
        ((smsCount.toFloat() / (smsCount + errorCount)) * 100).toInt()
    } else 100
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Status Card
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically { -it / 2 },
                    exit = fadeOut() + slideOutVertically { -it / 2 }
                ) {
                    HeroStatusCard(
                        isOnline = isListening,
                        messagesCount = smsCount,
                        successRate = successRate,
                        pendingCount = errorCount,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
            
            // Quick Actions Section
            item {
                SectionHeader(
                    title = "Quick Actions",
                    isDarkTheme = isDarkTheme
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "View Activity",
                        subtitle = "See all messages",
                        icon = Icons.Default.Timeline,
                        onClick = { /* Navigate to activity */ },
                        modifier = Modifier.weight(1f),
                        isDarkTheme = isDarkTheme,
                        gradientColors = GradientColors.accentGradient
                    )
                    
                    QuickActionCard(
                        title = "Force Sync",
                        subtitle = "Manual refresh",
                        icon = Icons.Default.Storage,
                        onClick = { 
                            viewModel.appendLog("Manual sync triggered")
                            scope.launch {
                                snackbarHostState.showSnackbar("Syncing...")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        isDarkTheme = isDarkTheme,
                        gradientColors = GradientColors.successGradient
                    )
                }
            }
            
            // Recent Activity Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "Recent Activity",
                        isDarkTheme = isDarkTheme
                    )
                    
                    Row {
                        TextButton(onClick = {
                            val clipboard = context.getSystemService<ClipboardManager>()
                            val clip = android.content.ClipData.newPlainText("SMSGateway Logs", logs)
                            clipboard?.setPrimaryClip(clip)
                            scope.launch {
                                snackbarHostState.showSnackbar("Logs copied to clipboard")
                            }
                        }) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = ThemeColors.textSecondary(isDarkTheme)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Copy",
                                color = ThemeColors.textSecondary(isDarkTheme)
                            )
                        }
                        TextButton(onClick = {
                            viewModel.clearLogs()
                            scope.launch {
                                snackbarHostState.showSnackbar("Logs cleared")
                            }
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = ThemeColors.textSecondary(isDarkTheme)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Clear",
                                color = ThemeColors.textSecondary(isDarkTheme)
                            )
                        }
                    }
                }
            }
            
            // Activity List
            item {
                if (logs.isBlank()) {
                    // Empty state
                    GlassCard(isDarkTheme = isDarkTheme) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Sms,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = ThemeColors.textSecondary(isDarkTheme).copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No activity yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = ThemeColors.textSecondary(isDarkTheme)
                            )
                            Text(
                                "Messages will appear here when received",
                                style = MaterialTheme.typography.bodySmall,
                                color = ThemeColors.textSecondary(isDarkTheme).copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    // Parse logs and display as cards
                    val logLines = logs.split("\n").filter { it.isNotBlank() }.takeLast(10)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        logLines.forEachIndexed { index, logLine ->
                            val isError = logLine.contains("error", ignoreCase = true) ||
                                    logLine.contains("failed", ignoreCase = true)
                            val isPending = logLine.contains("pending", ignoreCase = true) ||
                                    logLine.contains("processing", ignoreCase = true)
                            
                            ActivityCard(
                                title = when {
                                    isError -> "Error"
                                    isPending -> "Processing"
                                    else -> "SMS Processed"
                                },
                                subtitle = logLine.take(60) + if (logLine.length > 60) "..." else "",
                                timestamp = "Just now",
                                status = when {
                                    isError -> PulseStatus.ERROR
                                    isPending -> PulseStatus.PENDING
                                    else -> PulseStatus.SUCCESS
                                },
                                isDarkTheme = isDarkTheme,
                                animationDelay = index * 100
                            )
                        }
                    }
                }
            }
            
            // Bottom spacer for navigation bar
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        
        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Hero status card with connection status, stats, and gradient overlay.
 */
@Composable
private fun HeroStatusCard(
    isOnline: Boolean,
    messagesCount: Int,
    successRate: Int,
    pendingCount: Int,
    isDarkTheme: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero")
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    GlassCard(
        isDarkTheme = isDarkTheme,
        gradientOverlay = if (isOnline) GradientColors.accentGradient else GradientColors.errorGradient
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // WiFi icon with pulse
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = ThemeColors.textPrimary(isDarkTheme),
                            modifier = Modifier.size(32.dp)
                        )
                        if (isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .offset(x = 2.dp, y = (-2).dp)
                            ) {
                                ConnectionPulse(isConnected = true, size = 12)
                            }
                        }
                    }
                    
                    Column {
                        Text(
                            text = if (isOnline) "Online" else "Offline",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = ThemeColors.textPrimary(isDarkTheme)
                        )
                        Text(
                            text = if (isOnline) "Device is syncing smoothly" else "Check your connection",
                            style = MaterialTheme.typography.bodySmall,
                            color = ThemeColors.textSecondary(isDarkTheme)
                        )
                    }
                }
                
                // Toggle button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ThemeColors.glassBackground(isDarkTheme))
                        .clickable { /* Toggle connection */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = "Toggle",
                        tint = if (isOnline) Success else Error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Messages Today",
                    value = messagesCount.toString(),
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Success Rate",
                    value = "$successRate%",
                    isDarkTheme = isDarkTheme,
                    valueColor = when {
                        successRate >= 90 -> Success
                        successRate >= 70 -> Warning
                        else -> Error
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = ThemeColors.textSecondary(isDarkTheme),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Last sync: Just now",
                        style = MaterialTheme.typography.labelSmall,
                        color = ThemeColors.textSecondary(isDarkTheme)
                    )
                }
                
                if (pendingCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(GradientColors.warmGradient)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$pendingCount Pending",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Stat card for the stats grid.
 */
@Composable
private fun StatCard(
    label: String,
    value: String,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    valueColor: Color? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ThemeColors.glassBackground(isDarkTheme))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ThemeColors.textSecondary(isDarkTheme)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor ?: ThemeColors.textPrimary(isDarkTheme)
            )
        }
    }
}

/**
 * Section header with gradient accent bar.
 */
@Composable
private fun SectionHeader(
    title: String,
    isDarkTheme: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(BrandPrimary, BrandSecondary)
                    )
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ThemeColors.textPrimary(isDarkTheme)
        )
    }
}

/**
 * Activity card for recent SMS events with slide-in animation.
 */
@Composable
private fun ActivityCard(
    title: String,
    subtitle: String,
    timestamp: String,
    status: PulseStatus,
    isDarkTheme: Boolean,
    animationDelay: Int = 0
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300, delayMillis = animationDelay),
        label = "alpha"
    )
    
    val animatedOffset by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(300, delayMillis = animationDelay, easing = FastOutSlowInEasing),
        label = "offset"
    )
    
    val statusGradient = when (status) {
        PulseStatus.SUCCESS -> GradientColors.successGradient
        PulseStatus.PENDING -> GradientColors.warmGradient
        PulseStatus.ERROR -> GradientColors.errorGradient
    }
    
    val statusIcon = when (status) {
        PulseStatus.SUCCESS -> Icons.Default.Check
        PulseStatus.PENDING -> Icons.Default.AccessTime
        PulseStatus.ERROR -> Icons.Default.Error
    }
    
    GlassCard(
        isDarkTheme = isDarkTheme,
        enableHover = true,
        onClick = { /* View details */ },
        modifier = Modifier
            .alpha(animatedAlpha)
            .scale(0.95f + (0.05f * animatedAlpha))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(statusGradient)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = status.name,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ThemeColors.textPrimary(isDarkTheme)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = ThemeColors.textPrimary(isDarkTheme),
                        maxLines = 1
                    )
                    Text(
                        text = timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = ThemeColors.textSecondary(isDarkTheme)
                    )
                }
            }
            
            // Status pulse and chevron
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPulse(status = status, size = 12)
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ThemeColors.textSecondary(isDarkTheme),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
