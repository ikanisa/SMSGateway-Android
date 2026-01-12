package com.ikanisa.smsgateway.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikanisa.smsgateway.ui.theme.BrandPrimary
import com.ikanisa.smsgateway.ui.theme.BrandSecondary
import com.ikanisa.smsgateway.ui.theme.GradientColors
import com.ikanisa.smsgateway.ui.theme.Success
import kotlinx.coroutines.delay

/**
 * Animated metric widget with spring-animated counter and radial progress.
 */
@Composable
fun AnimatedMetricWidget(
    title: String,
    value: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = GradientColors.primaryGradient,
    maxValue: Int = 100,
    showProgress: Boolean = true
) {
    // Animate the displayed value with spring physics
    var displayedValue by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(value) {
        // Animate from current displayed value to new value
        val start = displayedValue
        val duration = 1000L
        val startTime = System.currentTimeMillis()
        
        while (displayedValue != value) {
            val elapsed = System.currentTimeMillis() - startTime
            val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
            val easedProgress = FastOutSlowInEasing.transform(progress)
            displayedValue = (start + (value - start) * easedProgress).toInt()
            
            if (progress >= 1f) {
                displayedValue = value
                break
            }
            delay(16) // ~60fps
        }
    }
    
    val progress = if (maxValue > 0) (value.toFloat() / maxValue).coerceIn(0f, 1f) else 0f
    
    Gradient3DCard(
        modifier = modifier,
        gradientColors = gradientColors
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radial progress indicator
            if (showProgress) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedRadialProgress(
                        progress = progress,
                        gradientColors = listOf(Color.White, Color.White.copy(alpha = 0.6f)),
                        size = 64.dp
                    )
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = displayedValue.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
            }
        }
    }
}

/**
 * Radial progress indicator with gradient stroke.
 */
@Composable
fun AnimatedRadialProgress(
    progress: Float,
    gradientColors: List<Color>,
    size: Dp = 64.dp,
    strokeWidth: Dp = 6.dp
) {
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = 100f
            )
        )
    }
    
    Canvas(modifier = Modifier.size(size)) {
        val strokeWidthPx = strokeWidth.toPx()
        val radius = (this.size.minDimension - strokeWidthPx) / 2
        val center = Offset(this.size.width / 2, this.size.height / 2)
        
        // Background circle
        drawCircle(
            color = Color.White.copy(alpha = 0.2f),
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )
        
        // Progress arc
        drawArc(
            brush = Brush.sweepGradient(gradientColors),
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress.value,
            useCenter = false,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )
    }
}

/**
 * Compact stat card for quick metrics display.
 */
@Composable
fun CompactStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accentColor: Color = BrandPrimary
) {
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        enableShimmer = false
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.3f),
                                accentColor.copy(alpha = 0.1f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Live status indicator with animated rings.
 */
@Composable
fun LiveStatusIndicator(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = Success,
    inactiveColor: Color = MaterialTheme.colorScheme.error,
    size: Dp = 80.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "statusPulse")
    
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1"
    )
    
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2"
    )
    
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = if (isActive) 0f else 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1Alpha"
    )
    
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isActive) 0f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2Alpha"
    )
    
    val color by animateColorAsState(
        targetValue = if (isActive) activeColor else inactiveColor,
        animationSpec = tween(300),
        label = "statusColor"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Animated rings
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(size / 2)
                    .scale(ring1Scale)
                    .background(
                        color = color.copy(alpha = ring1Alpha),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(size / 2)
                    .scale(ring2Scale)
                    .background(
                        color = color.copy(alpha = ring2Alpha),
                        shape = CircleShape
                    )
            )
        }
        
        // Center dot
        Box(
            modifier = Modifier
                .size(size / 2)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color,
                            color.copy(alpha = 0.7f)
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}
