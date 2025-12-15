package com.example.smsgateway.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatusCard(
    isListening: Boolean,
    isConfigured: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    // Pulsing scale animation for the dot
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Pulsing alpha animation for glow effect
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val dotColor by animateColorAsState(
        targetValue = if (isListening) Color(0xFF4CAF50) else Color(0xFFf44336),
        animationSpec = tween(300),
        label = "dotColor"
    )
    
    val statusColor by animateColorAsState(
        targetValue = if (isConfigured) Color(0xFF4CAF50) else Color(0xFFFF9800),
        animationSpec = tween(300),
        label = "statusColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated status indicator with glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(48.dp)
            ) {
                // Glow effect (outer circle)
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .scale(scale)
                            .alpha(alpha)
                            .background(color = dotColor.copy(alpha = 0.3f), shape = CircleShape)
                    )
                }
                // Main dot
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(color = dotColor, shape = CircleShape)
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                AnimatedContent(
                    targetState = isListening,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(200)) + slideInVertically { -it / 2 })
                            .togetherWith(fadeOut(animationSpec = tween(200)) + slideOutVertically { it / 2 })
                    },
                    label = "statusText"
                ) { listening ->
                    Text(
                        text = if (listening) "Listening" else "Stopped",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Forwarding incoming SMS to Supabase",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (isConfigured) "Configured ✅" else "Not configured ⚠️",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    // Animate number changes
    val animatedValue by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 100f),
        label = "metricValue"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            AnimatedContent(
                targetState = animatedValue.toInt(),
                transitionSpec = {
                    if (targetState > initialState) {
                        (fadeIn(animationSpec = tween(150)) + slideInVertically { -it })
                            .togetherWith(fadeOut(animationSpec = tween(150)) + slideOutVertically { it })
                    } else {
                        (fadeIn(animationSpec = tween(150)) + slideInVertically { it })
                            .togetherWith(fadeOut(animationSpec = tween(150)) + slideOutVertically { -it })
                    }
                },
                label = "metricNumber"
            ) { targetCount ->
                Text(
                    text = targetCount.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
