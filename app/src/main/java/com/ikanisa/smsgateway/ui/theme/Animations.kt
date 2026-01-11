package com.ikanisa.smsgateway.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Animation constants and presets for consistent app-wide animations.
 */
object AnimationDefaults {
    // Duration constants
    const val FAST_DURATION = 150
    const val MEDIUM_DURATION = 300
    const val SLOW_DURATION = 500
    const val VERY_SLOW_DURATION = 800
    
    // Spring presets
    val SpringBouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val SpringSnappy = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val SpringGentle = spring<Float>(
        dampingRatio = Spring.DampingRatioHighBouncy,
        stiffness = Spring.StiffnessVeryLow
    )
    
    // Tween presets
    val TweenFast: AnimationSpec<Float> = tween(FAST_DURATION)
    val TweenMedium: AnimationSpec<Float> = tween(MEDIUM_DURATION)
    val TweenSlow: AnimationSpec<Float> = tween(SLOW_DURATION)
    
    // Easing presets (commonly used curves)
    object Easing {
        val EaseOut = androidx.compose.animation.core.FastOutSlowInEasing
        val EaseIn = androidx.compose.animation.core.LinearOutSlowInEasing
        val EaseInOut = androidx.compose.animation.core.FastOutLinearInEasing
        val Linear = androidx.compose.animation.core.LinearEasing
    }
}

/**
 * Common UI dimension constants.
 */
object Dimensions {
    // Corner radius
    const val CORNER_SMALL = 8
    const val CORNER_MEDIUM = 16
    const val CORNER_LARGE = 24
    const val CORNER_EXTRA_LARGE = 32
    
    // Elevation
    const val ELEVATION_NONE = 0
    const val ELEVATION_SMALL = 2
    const val ELEVATION_MEDIUM = 4
    const val ELEVATION_LARGE = 8
    const val ELEVATION_EXTRA_LARGE = 16
    
    // Spacing
    const val SPACING_TINY = 4
    const val SPACING_SMALL = 8
    const val SPACING_MEDIUM = 16
    const val SPACING_LARGE = 24
    const val SPACING_EXTRA_LARGE = 32
    const val SPACING_HUGE = 48
    
    // Icon sizes
    const val ICON_SMALL = 16
    const val ICON_MEDIUM = 24
    const val ICON_LARGE = 32
    const val ICON_EXTRA_LARGE = 48
}
