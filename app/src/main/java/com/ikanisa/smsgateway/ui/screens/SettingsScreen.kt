package com.ikanisa.smsgateway.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ikanisa.smsgateway.AppDefaults
import com.ikanisa.smsgateway.ui.components.GlassCard
import com.ikanisa.smsgateway.ui.components.SettingButton
import com.ikanisa.smsgateway.ui.components.SettingToggle
import com.ikanisa.smsgateway.ui.theme.BrandAccent
import com.ikanisa.smsgateway.ui.theme.BrandPrimary
import com.ikanisa.smsgateway.ui.theme.BrandSecondary
import com.ikanisa.smsgateway.ui.theme.GradientColors
import com.ikanisa.smsgateway.ui.theme.Success
import com.ikanisa.smsgateway.ui.theme.ThemeColors

/**
 * Settings screen with toggles, theme switcher, and about section.
 * Matches the React component design with glassmorphism cards.
 */
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Local settings state
    var autoSync by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf(true) }
    var hapticFeedback by remember { mutableStateOf(true) }
    var biometricLock by remember { mutableStateOf(false) }
    
    val momoCode = AppDefaults.MOMO_CODE.ifEmpty { "Not Set" }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically { -it / 2 }
            ) {
                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = ThemeColors.textPrimary(isDarkTheme)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Manage your SMS Gateway preferences",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ThemeColors.textSecondary(isDarkTheme)
                    )
                }
            }
        }
        
        // Theme Toggle Card
        item {
            GlassCard(isDarkTheme = isDarkTheme) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.linearGradient(GradientColors.accentGradient)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Theme",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Theme",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ThemeColors.textPrimary(isDarkTheme)
                            )
                            Text(
                                text = if (isDarkTheme) "Dark mode" else "Light mode",
                                style = MaterialTheme.typography.bodySmall,
                                color = ThemeColors.textSecondary(isDarkTheme)
                            )
                        }
                    }
                    
                    Button(
                        onClick = onThemeToggle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.horizontalGradient(GradientColors.accentGradient)
                            )
                    ) {
                        Text(
                            text = "Switch",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // General Section Header
        item {
            SectionTitle(
                title = "General",
                isDarkTheme = isDarkTheme
            )
        }
        
        // General Settings
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingToggle(
                    label = "Auto Sync",
                    description = "Automatically sync messages",
                    value = autoSync,
                    onValueChange = { autoSync = it },
                    icon = Icons.Default.Storage,
                    isDarkTheme = isDarkTheme
                )
                
                SettingToggle(
                    label = "Notifications",
                    description = "Get notified on new messages",
                    value = notifications,
                    onValueChange = { notifications = it },
                    icon = Icons.Default.Notifications,
                    isDarkTheme = isDarkTheme
                )
                
                SettingToggle(
                    label = "Haptic Feedback",
                    description = "Vibrate on interactions",
                    value = hapticFeedback,
                    onValueChange = { hapticFeedback = it },
                    icon = Icons.Default.PhoneAndroid,
                    isDarkTheme = isDarkTheme
                )
            }
        }
        
        // Security Section Header
        item {
            SectionTitle(
                title = "Security",
                isDarkTheme = isDarkTheme
            )
        }
        
        // Security Settings
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingToggle(
                    label = "Biometric Lock",
                    description = "Use fingerprint to unlock",
                    value = biometricLock,
                    onValueChange = { biometricLock = it },
                    icon = Icons.Default.Security,
                    isDarkTheme = isDarkTheme
                )
                
                SettingButton(
                    label = "Device Credentials",
                    description = "Manage API keys and secrets",
                    icon = Icons.Default.Shield,
                    onClick = { /* Navigate to credentials */ },
                    isDarkTheme = isDarkTheme
                )
                
                SettingButton(
                    label = "Connection Status",
                    description = "View Supabase connection health",
                    icon = Icons.Default.SignalCellularAlt,
                    onClick = { /* Show connection details */ },
                    badge = "Healthy",
                    isDarkTheme = isDarkTheme,
                    gradientColors = GradientColors.successGradient
                )
            }
        }
        
        // About Section Header
        item {
            SectionTitle(
                title = "About",
                isDarkTheme = isDarkTheme
            )
        }
        
        // About Card
        item {
            GlassCard(isDarkTheme = isDarkTheme) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AboutRow(
                        label = "Version",
                        value = "1.0.0",
                        isDarkTheme = isDarkTheme
                    )
                    AboutRow(
                        label = "MOMO Code",
                        value = momoCode,
                        isDarkTheme = isDarkTheme,
                        isMono = true
                    )
                    AboutRow(
                        label = "Last Updated",
                        value = "Jan 11, 2026",
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
        
        // Bottom spacer for navigation bar
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    isDarkTheme: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
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
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ThemeColors.textPrimary(isDarkTheme)
        )
    }
}

@Composable
private fun AboutRow(
    label: String,
    value: String,
    isDarkTheme: Boolean,
    isMono: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = ThemeColors.textSecondary(isDarkTheme)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
            color = ThemeColors.textPrimary(isDarkTheme)
        )
    }
}
