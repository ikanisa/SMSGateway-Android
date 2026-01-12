package com.ikanisa.smsgateway.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ikanisa.smsgateway.ui.theme.ThemeColors

/**
 * Premium glassmorphism card matching the React design.
 * Features semi-transparent background, subtle border, and shadow effects.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    cornerRadius: Dp = 24.dp,
    enableHover: Boolean = false,
    gradientOverlay: List<Color>? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Scale animation for press effect
    val scale = if (isPressed && enableHover) 0.98f else 1f
    
    val glassBackground = ThemeColors.glassBackground(isDarkTheme)
    val glassBorder = ThemeColors.glassBorder(isDarkTheme)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                color = glassBackground,
                shape = RoundedCornerShape(cornerRadius)
            )
            .then(
                if (gradientOverlay != null) {
                    Modifier.background(
                        brush = Brush.linearGradient(gradientOverlay.map { it.copy(alpha = 0.1f) }),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                } else Modifier
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(20.dp),
        content = content
    )
}

/**
 * Glassmorphism surface with subtle blur effect.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val glassBackground = ThemeColors.glassBackground(isDarkTheme)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                color = glassBackground,
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp),
        content = content
    )
}
