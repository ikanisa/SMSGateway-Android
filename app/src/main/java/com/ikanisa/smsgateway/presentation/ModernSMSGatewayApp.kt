package com.ikanisa.smsgateway.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ikanisa.smsgateway.presentation.components.LiquidGlassCard
import com.ikanisa.smsgateway.presentation.screens.home.HomeScreen
import com.ikanisa.smsgateway.presentation.screens.settings.ModernSettingsScreen
import com.ikanisa.smsgateway.presentation.theme.SMSGatewayTheme

/**
 * Navigation destinations for the modern app
 */
enum class ModernNavDestination {
    HOME, SETTINGS
}

/**
 * Modern SMS Gateway App with Liquid Glass design.
 * Uses the new presentation layer components.
 */
@Composable
fun ModernSMSGatewayApp() {
    var currentDestination by rememberSaveable { mutableStateOf(ModernNavDestination.HOME) }
    
    SMSGatewayTheme {
        // Animated background
        val infiniteTransition = rememberInfiniteTransition(label = "bgAnim")
        val bgOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 50f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bgFloat"
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F172A), // Slate-900
                            Color(0xFF1E1B4B)  // Indigo-950
                        )
                    )
                )
        ) {
            // Decorative gradient orbs - Liquid Glass style
            Box(
                modifier = Modifier
                    .size(350.dp)
                    .offset(x = (200 + bgOffset).dp, y = (-50 - bgOffset / 2).dp)
                    .blur(120.dp)
                    .alpha(0.4f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = 0.4f), // Primary
                                Color(0xFF8B5CF6).copy(alpha = 0.2f), // Secondary
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(x = (-100 - bgOffset / 2).dp, y = (400 + bgOffset).dp)
                    .blur(100.dp)
                    .alpha(0.3f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF06B6D4).copy(alpha = 0.3f), // Cyan
                                Color(0xFF6366F1).copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .offset(x = (50 + bgOffset / 3).dp, y = (700 - bgOffset / 2).dp)
                    .blur(80.dp)
                    .alpha(0.25f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF10B981).copy(alpha = 0.3f), // Success
                                Color(0xFF06B6D4).copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            // Screen content with animated transitions
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                modifier = Modifier.fillMaxSize(),
                label = "screenTransition"
            ) { destination ->
                when (destination) {
                    ModernNavDestination.HOME -> {
                        HomeScreen(
                            onNavigateToSettings = { 
                                currentDestination = ModernNavDestination.SETTINGS 
                            }
                        )
                    }
                    ModernNavDestination.SETTINGS -> {
                        ModernSettingsScreen(
                            onNavigateBack = {
                                currentDestination = ModernNavDestination.HOME
                            }
                        )
                    }
                }
            }
            
            // Modern bottom navigation (only show on Home)
            if (currentDestination == ModernNavDestination.HOME) {
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    ModernBottomNavBar(
                        currentDestination = currentDestination,
                        onDestinationChange = { currentDestination = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernBottomNavBar(
    currentDestination: ModernNavDestination,
    onDestinationChange: (ModernNavDestination) -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Outlined.Home,
                label = "Home",
                selected = currentDestination == ModernNavDestination.HOME,
                onClick = { onDestinationChange(ModernNavDestination.HOME) }
            )
            NavItem(
                icon = Icons.Outlined.Settings,
                label = "Settings",
                selected = currentDestination == ModernNavDestination.SETTINGS,
                onClick = { onDestinationChange(ModernNavDestination.SETTINGS) }
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
