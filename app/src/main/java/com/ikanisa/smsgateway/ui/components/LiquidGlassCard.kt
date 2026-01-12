package com.ikanisa.smsgateway.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ikanisa.smsgateway.ui.theme.BrandPrimary
import com.ikanisa.smsgateway.ui.theme.BrandSecondary
import com.ikanisa.smsgateway.ui.theme.Depth3DDeep
import com.ikanisa.smsgateway.ui.theme.Depth3DShadow
import com.ikanisa.smsgateway.ui.theme.GradientColors
import com.ikanisa.smsgateway.ui.theme.LiquidGlassBase
import com.ikanisa.smsgateway.ui.theme.LiquidGlassBorder
import com.ikanisa.smsgateway.ui.theme.LiquidGlassDarkBase
import com.ikanisa.smsgateway.ui.theme.LiquidGlassDarkBorder
import com.ikanisa.smsgateway.ui.theme.LiquidGlassDarkHighlight
import com.ikanisa.smsgateway.ui.theme.LiquidGlassDarkMid
import com.ikanisa.smsgateway.ui.theme.LiquidGlassHighlight
import com.ikanisa.smsgateway.ui.theme.LiquidGlassMid
import com.ikanisa.smsgateway.ui.theme.LiquidGlassShine

/**
 * Premium Liquid Glass Card with 3D depth effects.
 * Features multi-layered glassmorphism with animated shimmer and depth shadows.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    enableShimmer: Boolean = true,
    gradientColors: List<Color> = GradientColors.liquidGlass3D,
    content: @Composable BoxScope.() -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == MaterialTheme.colorScheme.surface
    val shape = RoundedCornerShape(cornerRadius)
    
    // Animated shimmer effect
    val infiniteTransition = rememberInfiniteTransition(label = "liquidShimmer")
    val shimmerPosition by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerPos"
    )
    
    // Outer container with 3D shadow
    Box(
        modifier = modifier
            .shadow(
                elevation = 24.dp,
                shape = shape,
                ambientColor = gradientColors.first().copy(alpha = 0.3f),
                spotColor = gradientColors.last().copy(alpha = 0.2f)
            )
    ) {
        // Background blur layer (simulated with gradient)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                gradientColors.first().copy(alpha = 0.15f),
                                gradientColors.last().copy(alpha = 0.05f)
                            )
                        } else {
                            listOf(
                                gradientColors.first().copy(alpha = 0.1f),
                                gradientColors.last().copy(alpha = 0.05f)
                            )
                        }
                    )
                )
                .blur(20.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        )
        
        // Main glass surface
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                LiquidGlassDarkHighlight,
                                LiquidGlassDarkMid,
                                LiquidGlassDarkBase
                            )
                        } else {
                            listOf(
                                LiquidGlassHighlight,
                                LiquidGlassMid,
                                LiquidGlassBase
                            )
                        }
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                LiquidGlassDarkBorder,
                                Color.Transparent,
                                LiquidGlassDarkBorder.copy(alpha = 0.1f)
                            )
                        } else {
                            listOf(
                                LiquidGlassBorder,
                                Color.Transparent,
                                LiquidGlassBorder.copy(alpha = 0.3f)
                            )
                        },
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    ),
                    shape = shape
                )
        )
        
        // Shimmer overlay
        if (enableShimmer) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .graphicsLayer { alpha = 0.4f }
                    .drawBehind {
                        val shimmerWidth = size.width * 0.5f
                        val startX = size.width * shimmerPosition
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    LiquidGlassShine.copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                start = Offset(startX - shimmerWidth, 0f),
                                end = Offset(startX + shimmerWidth, size.height)
                            )
                        )
                    }
            )
        }
        
        // Content layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            content = content
        )
    }
}

/**
 * 3D Gradient Card with layered depth effect for metrics and stats.
 */
@Composable
fun Gradient3DCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(BrandPrimary, BrandSecondary),
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    // Animate subtle rotation for 3D effect
    val infiniteTransition = rememberInfiniteTransition(label = "3dFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )
    
    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = floatOffset
                shadowElevation = 16f + floatOffset
            }
            .shadow(
                elevation = 20.dp,
                shape = shape,
                ambientColor = gradientColors.first().copy(alpha = 0.4f),
                spotColor = gradientColors.last().copy(alpha = 0.3f)
            )
    ) {
        // Deep shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { translationY = 8f }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Depth3DDeep,
                            Color.Transparent
                        )
                    ),
                    shape = shape
                )
        )
        
        // Main gradient background
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        )
        
        // Top highlight for 3D depth
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.Transparent,
                            Depth3DShadow
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = shape
                )
        )
        
        // Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            content = content
        )
    }
}

/**
 * Frosted glass surface for overlays and navigation bars.
 */
@Composable
fun FrostedGlassSurface(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == MaterialTheme.colorScheme.surface
    val shape = RoundedCornerShape(20.dp)
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                color = if (isDarkTheme) {
                    Color.Black.copy(alpha = 0.5f)
                } else {
                    Color.White.copy(alpha = 0.7f)
                }
            )
            .blur(blurRadius)
            .border(
                width = 1.dp,
                color = if (isDarkTheme) {
                    Color.White.copy(alpha = 0.1f)
                } else {
                    Color.White.copy(alpha = 0.5f)
                },
                shape = shape
            )
            .padding(16.dp),
        content = content
    )
}
