package com.ikanisa.smsgateway.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ikanisa.smsgateway.ui.theme.GradientColors
import com.ikanisa.smsgateway.ui.theme.ThemeColors

/**
 * Navigation destination enum
 */
enum class NavDestination {
    HOME,
    SETTINGS
}

/**
 * Floating bottom navigation bar with glassmorphism effect.
 * Matches the React design with gradient active state.
 */
@Composable
fun BottomNavBar(
    currentDestination: NavDestination,
    onDestinationChange: (NavDestination) -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(ThemeColors.glassBackground(isDarkTheme))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = currentDestination == NavDestination.HOME,
                    onClick = { onDestinationChange(NavDestination.HOME) },
                    isDarkTheme = isDarkTheme
                )
                
                NavBarItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    isSelected = currentDestination == NavDestination.SETTINGS,
                    onClick = { onDestinationChange(NavDestination.SETTINGS) },
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

/**
 * Individual navigation bar item with animated selection state.
 */
@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.9f,
        animationSpec = tween(200),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (isSelected) {
                    Modifier.background(
                        brush = Brush.horizontalGradient(GradientColors.accentGradient)
                    )
                } else {
                    Modifier.background(Color.Transparent)
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 32.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) {
                Color.White
            } else {
                ThemeColors.textSecondary(isDarkTheme)
            },
            modifier = Modifier.size(24.dp)
        )
    }
}
