package com.ikanisa.smsgateway.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

// Color scheme with gradient support
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6366F1),      // Indigo-500
    primaryContainer = Color(0xFF4F46E5), // Indigo-600
    secondary = Color(0xFF8B5CF6),     // Purple-500
    secondaryContainer = Color(0xFF7C3AED), // Purple-600
    tertiary = Color(0xFF06B6D4),      // Cyan-500
    background = Color(0xFF0F172A),    // Slate-900
    surface = Color(0xFF1E293B),       // Slate-800
    surfaceVariant = Color(0xFF334155), // Slate-700
    error = Color(0xFFEF4444),         // Red-500
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF1F5F9),  // Slate-100
    onSurface = Color(0xFFE2E8F0),     // Slate-200
)

// Gradient definitions
object AppGradients {
    val PrimaryGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6366F1),
            Color(0xFF8B5CF6)
        )
    )
    
    val SurfaceGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E1B4B),
            Color(0xFF312E81)
        )
    )
    
    val GlassBackground = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.1f),
            Color.White.copy(alpha = 0.05f)
        )
    )
    
    val SuccessGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF10B981), Color(0xFF059669))
    )
    
    val ErrorGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626))
    )
}

// Typography with SF Pro/Inter font family
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Shape system with rounded corners
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun SMSGatewayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme, // Always dark for modern look
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
