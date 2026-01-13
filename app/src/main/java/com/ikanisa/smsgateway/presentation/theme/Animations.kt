package com.ikanisa.smsgateway.presentation.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import kotlin.time.Duration.Companion.milliseconds

object AppAnimations {
    // Duration presets
    val FastDuration = 150.milliseconds
    val MediumDuration = 300.milliseconds
    val SlowDuration = 500.milliseconds
    
    // Spring specifications
    val SpringSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val BounceSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    // Easing curves
    val EaseInOutCubic = CubicBezierEasing(0.645f, 0.045f, 0.355f, 1.0f)
    val EaseOutExpo = CubicBezierEasing(0.19f, 1.0f, 0.22f, 1.0f)
    
    // Enter/Exit transitions
    fun slideInFromBottom() = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(300, easing = EaseOutExpo)
    ) + fadeIn(animationSpec = tween(200))
    
    fun slideOutToBottom() = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(300, easing = EaseInOutCubic)
    ) + fadeOut(animationSpec = tween(200))
    
    fun scaleIn() = scaleIn(
        initialScale = 0.8f,
        animationSpec = SpringSpec
    ) + fadeIn()
    
    fun scaleOut() = scaleOut(
        targetScale = 0.8f,
        animationSpec = SpringSpec
    ) + fadeOut()
}
