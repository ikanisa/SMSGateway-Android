package com.ikanisa.smsgateway.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

/**
 * A glassmorphic toggle switch with smooth animations.
 */
@Composable
fun GlassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    
    // Animated values
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 0.dp,
        animationSpec = tween(200),
        label = "thumbOffset"
    )
    
    val trackColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF6366F1) else Color.White.copy(alpha = 0.1f),
        animationSpec = tween(200),
        label = "trackColor"
    )
    
    val thumbScale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.9f,
        animationSpec = tween(150),
        label = "thumbScale"
    )
    
    val glowAlpha by animateFloatAsState(
        targetValue = if (checked) 0.4f else 0f,
        animationSpec = tween(200),
        label = "glowAlpha"
    )
    
    Box(
        modifier = modifier
            .width(56.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCheckedChange(!checked)
                }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // Glow effect when on
        if (checked) {
            Box(
                modifier = Modifier
                    .size(56.dp, 32.dp)
                    .blur(12.dp)
                    .background(
                        color = Color(0xFF6366F1).copy(alpha = glowAlpha),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
        }
        
        // Track
        Box(
            modifier = Modifier
                .size(56.dp, 32.dp)
                .background(
                    color = trackColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        )
        
        // Thumb
        Box(
            modifier = Modifier
                .padding(4.dp)
                .offset(x = thumbOffset)
                .scale(thumbScale)
                .size(24.dp)
                .background(
                    brush = if (checked) {
                        Brush.linearGradient(
                            listOf(Color.White, Color(0xFFE0E7FF))
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.7f))
                        )
                    },
                    shape = CircleShape
                )
        )
    }
}
