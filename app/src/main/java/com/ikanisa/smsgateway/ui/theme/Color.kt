package com.ikanisa.smsgateway.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// Brand Colors - Modern Indigo/Purple palette for premium feel
// =============================================================================
val BrandPrimary = Color(0xFF6366F1)      // Indigo
val BrandSecondary = Color(0xFF8B5CF6)    // Purple
val BrandAccent = Color(0xFF06B6D4)       // Cyan
val BrandGold = Color(0xFFF59E0B)         // Amber/Gold

// =============================================================================
// Semantic Colors
// =============================================================================
val Success = Color(0xFF10B981)           // Emerald Green
val SuccessLight = Color(0xFFD1FAE5)      // Light Green
val Warning = Color(0xFFF59E0B)           // Amber
val WarningLight = Color(0xFFFEF3C7)      // Light Amber
val Error = Color(0xFFEF4444)             // Red
val ErrorLight = Color(0xFFFEE2E2)        // Light Red
val Info = Color(0xFF3B82F6)              // Blue
val InfoLight = Color(0xFFDBEAFE)         // Light Blue

// =============================================================================
// Glassmorphism Colors
// =============================================================================
val GlassWhite = Color(0xCCFFFFFF)        // 80% white
val GlassWhiteLight = Color(0x99FFFFFF)   // 60% white
val GlassWhiteDark = Color(0x33FFFFFF)    // 20% white
val GlassBorder = Color(0x33FFFFFF)       // 20% white border
val GlassShadow = Color(0x1A000000)       // 10% black shadow

val GlassDark = Color(0x33000000)         // 20% black for dark mode
val GlassDarkMedium = Color(0x66000000)   // 40% black for dark mode

// =============================================================================
// Gradient Presets
// =============================================================================
object GradientColors {
    val primaryGradient = listOf(BrandPrimary, BrandSecondary)
    val accentGradient = listOf(BrandAccent, BrandPrimary)
    val successGradient = listOf(Success, Color(0xFF34D399))
    val warmGradient = listOf(Color(0xFFF97316), Color(0xFFEF4444))
    val coolGradient = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
    val purpleGradient = listOf(Color(0xFFA855F7), Color(0xFF6366F1))
    val sunsetGradient = listOf(Color(0xFFF472B6), Color(0xFFFB923C))
    
    // Background gradients
    val lightBackground = listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
    val darkBackground = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
}

// =============================================================================
// Material 3 Theme Colors (Enhanced)
// =============================================================================

// Light Theme
val md_theme_light_primary = Color(0xFF6366F1)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFE0E7FF)
val md_theme_light_onPrimaryContainer = Color(0xFF1E1B4B)
val md_theme_light_secondary = Color(0xFF8B5CF6)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFEDE9FE)
val md_theme_light_onSecondaryContainer = Color(0xFF2E1065)
val md_theme_light_tertiary = Color(0xFF06B6D4)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFCFFAFE)
val md_theme_light_onTertiaryContainer = Color(0xFF083344)
val md_theme_light_error = Color(0xFFEF4444)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFFEE2E2)
val md_theme_light_onErrorContainer = Color(0xFF7F1D1D)
val md_theme_light_background = Color(0xFFFAFAFA)
val md_theme_light_onBackground = Color(0xFF1F2937)
val md_theme_light_surface = Color(0xFFFFFFFF)
val md_theme_light_onSurface = Color(0xFF1F2937)
val md_theme_light_surfaceVariant = Color(0xFFF3F4F6)
val md_theme_light_onSurfaceVariant = Color(0xFF6B7280)
val md_theme_light_outline = Color(0xFFD1D5DB)
val md_theme_light_inverseOnSurface = Color(0xFFF9FAFB)
val md_theme_light_inverseSurface = Color(0xFF1F2937)
val md_theme_light_inversePrimary = Color(0xFFA5B4FC)
val md_theme_light_scrim = Color(0xFF000000)

// Dark Theme
val md_theme_dark_primary = Color(0xFFA5B4FC)
val md_theme_dark_onPrimary = Color(0xFF1E1B4B)
val md_theme_dark_primaryContainer = Color(0xFF4338CA)
val md_theme_dark_onPrimaryContainer = Color(0xFFE0E7FF)
val md_theme_dark_secondary = Color(0xFFC4B5FD)
val md_theme_dark_onSecondary = Color(0xFF2E1065)
val md_theme_dark_secondaryContainer = Color(0xFF6D28D9)
val md_theme_dark_onSecondaryContainer = Color(0xFFEDE9FE)
val md_theme_dark_tertiary = Color(0xFF67E8F9)
val md_theme_dark_onTertiary = Color(0xFF083344)
val md_theme_dark_tertiaryContainer = Color(0xFF0891B2)
val md_theme_dark_onTertiaryContainer = Color(0xFFCFFAFE)
val md_theme_dark_error = Color(0xFFFCA5A5)
val md_theme_dark_onError = Color(0xFF7F1D1D)
val md_theme_dark_errorContainer = Color(0xFFDC2626)
val md_theme_dark_onErrorContainer = Color(0xFFFEE2E2)
val md_theme_dark_background = Color(0xFF0F172A)
val md_theme_dark_onBackground = Color(0xFFF1F5F9)
val md_theme_dark_surface = Color(0xFF1E293B)
val md_theme_dark_onSurface = Color(0xFFF1F5F9)
val md_theme_dark_surfaceVariant = Color(0xFF334155)
val md_theme_dark_onSurfaceVariant = Color(0xFF94A3B8)
val md_theme_dark_outline = Color(0xFF475569)
val md_theme_dark_inverseOnSurface = Color(0xFF0F172A)
val md_theme_dark_inverseSurface = Color(0xFFF1F5F9)
val md_theme_dark_inversePrimary = Color(0xFF6366F1)
val md_theme_dark_scrim = Color(0xFF000000)
