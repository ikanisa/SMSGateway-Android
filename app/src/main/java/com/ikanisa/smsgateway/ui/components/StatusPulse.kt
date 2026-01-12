package com.ikanisa.smsgateway.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ikanisa.smsgateway.ui.theme.Error
import com.ikanisa.smsgateway.ui.theme.Success
import com.ikanisa.smsgateway.ui.theme.Warning

/**
 * Status types for the pulse indicator
 */
enum class PulseStatus {
    SUCCESS,
    PENDING,
    ERROR
}

/**
 * Animated pulsing status indicator matching the React StatusPulse component.
 * Features a solid dot with a pinging animation overlay.
 */
@Composable
fun StatusPulse(
    status: PulseStatus,
    modifier: Modifier = Modifier,
    size: Int = 12
) {
    val color = when (status) {
        PulseStatus.SUCCESS -> Success
        PulseStatus.PENDING -> Warning
        PulseStatus.ERROR -> Error
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    // Ping animation
    val pingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "pingScale"
    )
    
    val pingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "pingAlpha"
    )
    
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pinging circle
        Box(
            modifier = Modifier
                .size(size.dp)
                .scale(pingScale)
                .alpha(pingAlpha)
                .background(
                    color = color,
                    shape = CircleShape
                )
        )
        
        // Solid dot
        Box(
            modifier = Modifier
                .size(size.dp)
                .background(
                    color = color,
                    shape = CircleShape
                )
        )
    }
}

/**
 * Simple boolean-based status pulse for online/offline states.
 */
@Composable
fun ConnectionPulse(
    isConnected: Boolean,
    modifier: Modifier = Modifier,
    size: Int = 12
) {
    StatusPulse(
        status = if (isConnected) PulseStatus.SUCCESS else PulseStatus.ERROR,
        modifier = modifier,
        size = size
    )
}
