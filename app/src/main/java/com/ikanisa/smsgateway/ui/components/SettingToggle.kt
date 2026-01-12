package com.ikanisa.smsgateway.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ikanisa.smsgateway.ui.theme.GradientColors
import com.ikanisa.smsgateway.ui.theme.ThemeColors
import com.ikanisa.smsgateway.ui.theme.Warning

/**
 * Toggle setting row with icon, label, description, and animated switch.
 */
@Composable
fun SettingToggle(
    label: String,
    description: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    gradientColors: List<Color> = GradientColors.accentGradient
) {
    GlassCard(
        modifier = modifier,
        isDarkTheme = isDarkTheme,
        enableHover = true,
        onClick = { onValueChange(!value) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(gradientColors)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Label and description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ThemeColors.textPrimary(isDarkTheme)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ThemeColors.textSecondary(isDarkTheme)
                )
            }
            
            // Toggle switch
            AnimatedToggleSwitch(
                checked = value,
                onCheckedChange = onValueChange,
                isDarkTheme = isDarkTheme
            )
        }
    }
}

/**
 * Animated toggle switch matching React design.
 */
@Composable
fun AnimatedToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    gradientColors: List<Color> = GradientColors.accentGradient
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 0.dp,
        animationSpec = tween(200),
        label = "thumbOffset"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .width(56.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (checked) {
                    Brush.horizontalGradient(gradientColors)
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            ThemeColors.glassBackground(isDarkTheme),
                            ThemeColors.glassBackground(isDarkTheme)
                        )
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .offset(x = thumbOffset)
                .background(
                    color = Color.White,
                    shape = CircleShape
                )
        )
    }
}

/**
 * Setting button row with icon, label, description, and optional badge.
 */
@Composable
fun SettingButton(
    label: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    badge: String? = null,
    gradientColors: List<Color> = GradientColors.accentGradient
) {
    GlassCard(
        modifier = modifier,
        isDarkTheme = isDarkTheme,
        enableHover = true,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(gradientColors)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Label and description
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ThemeColors.textPrimary(isDarkTheme)
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(Warning, Warning.copy(alpha = 0.8f))
                                    )
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ThemeColors.textSecondary(isDarkTheme)
                )
            }
            
            // Chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = ThemeColors.textSecondary(isDarkTheme),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
