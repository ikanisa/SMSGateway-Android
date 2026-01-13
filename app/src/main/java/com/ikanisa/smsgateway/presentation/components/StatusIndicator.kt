package com.ikanisa.smsgateway.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.BlurredEdgeTreatment

@Composable
fun FluidStatusIndicator(
    status: ConnectionStatus,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Glow effect
            if (status == ConnectionStatus.SYNCING) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .scale(pulseScale)
                        .blur(8.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                        .background(
                            color = status.color.copy(alpha = glowAlpha),
                            shape = CircleShape
                        )
                )
            }
            
            // Main indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = status.color,
                        shape = CircleShape
                    )
            )
        }
        
        if (showLabel) {
            Text(
                text = status.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

enum class ConnectionStatus(val color: Color, val label: String) {
    CONNECTED(Color(0xFF10B981), "Connected"),
    SYNCING(Color(0xFFF59E0B), "Syncing"),
    OFFLINE(Color(0xFFEF4444), "Offline")
}
