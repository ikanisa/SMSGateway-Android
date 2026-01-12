package com.ikanisa.smsgateway.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// Brand Colors - Modern Violet/Purple palette for premium feel (matching React design)
// =============================================================================
val BrandPrimary = Color(0xFF6366F1)      // Indigo (violet-500)
val BrandSecondary = Color(0xFF8B5CF6)    // Purple (purple-500)
val BrandAccent = Color(0xFF06B6D4)       // Cyan
val BrandGold = Color(0xFFF59E0B)         // Amber/Gold

// Violet accents from React design
val VioletAccent = Color(0xFF8B5CF6)      // violet-500
val PurpleAccent = Color(0xFF7C3AED)      // purple-600

// =============================================================================
// Semantic Colors
// =============================================================================
val Success = Color(0xFF10B981)           // Emerald Green (emerald-500)
val SuccessLight = Color(0xFFD1FAE5)      // Light Green
val SuccessDark = Color(0xFF059669)       // Emerald-600
val Warning = Color(0xFFF59E0B)           // Amber (amber-500)
val WarningLight = Color(0xFFFEF3C7)      // Light Amber
val WarningDark = Color(0xFFD97706)       // Amber-600
val Error = Color(0xFFEF4444)             // Red (red-500)
val ErrorLight = Color(0xFFFEE2E2)        // Light Red
val ErrorDark = Color(0xFFDC2626)         // Red-600
val Info = Color(0xFF3B82F6)              // Blue
val InfoLight = Color(0xFFDBEAFE)         // Light Blue

// =============================================================================
// Glassmorphism Colors - Theme-aware glass effects
// =============================================================================
// Dark theme glass
val GlassWhite = Color(0xCCFFFFFF)        // 80% white
val GlassWhiteLight = Color(0x99FFFFFF)   // 60% white
val GlassWhiteDark = Color(0x33FFFFFF)    // 20% white
val GlassBorder = Color(0x1AFFFFFF)       // 10% white border
val GlassShadow = Color(0x1A000000)       // 10% black shadow
val GlassOverlay = Color(0x0DFFFFFF)      // 5% white overlay

// Light theme glass
val GlassDark = Color(0x0D000000)         // 5% black for light mode
val GlassDarkMedium = Color(0x1A000000)   // 10% black for light mode
val GlassDarkBorder = Color(0x1A000000)   // 10% black border

// =============================================================================
// Theme-aware glass color helper
// =============================================================================
object ThemeColors {
    fun glassBackground(isDark: Boolean) = if (isDark) Color(0x1AFFFFFF) else Color(0x0D000000)
    fun glassBackgroundHover(isDark: Boolean) = if (isDark) Color(0x26FFFFFF) else Color(0x1A000000)
    fun glassBorder(isDark: Boolean) = if (isDark) Color(0x1AFFFFFF) else Color(0x1A000000)
    fun textPrimary(isDark: Boolean) = if (isDark) Color.White else Color(0xFF0F172A)
    fun textSecondary(isDark: Boolean) = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
}

// =============================================================================
// Gradient Presets
// =============================================================================
object GradientColors {
    // Primary gradients matching React design
    val primaryGradient = listOf(BrandPrimary, BrandSecondary)
    val accentGradient = listOf(VioletAccent, PurpleAccent) // violet-500 to purple-600
    val successGradient = listOf(Success, Color(0xFF059669)) // emerald-500 to emerald-600
    val warmGradient = listOf(Color(0xFFF59E0B), Color(0xFFF97316)) // amber-500 to orange-500
    val errorGradient = listOf(Error, Color(0xFFF43F5E)) // red-500 to rose-500
    val coolGradient = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
    val purpleGradient = listOf(Color(0xFFA855F7), Color(0xFF6366F1))
    val sunsetGradient = listOf(Color(0xFFF472B6), Color(0xFFFB923C))
    
    // Background gradients
    val lightBackground = listOf(Color(0xFFF8FAFC), Color(0xFFFFFFFF), Color(0xFFF8FAFC))
    val darkBackground = listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF020617))
    
    // 3D Liquid Glass Gradients - Premium layered effects
    val liquidGlass3D = listOf(
        Color(0xFF667EEA),  // Deep indigo
        Color(0xFF764BA2),  // Rich purple
        Color(0xFFF093FB)   // Soft pink
    )
    val oceanDepth = listOf(
        Color(0xFF0093E9),  // Bright blue
        Color(0xFF80D0C7)   // Teal
    )
    val auroraGlow = listOf(
        Color(0xFFA855F7),  // Purple
        Color(0xFF6366F1),  // Indigo
        Color(0xFF06B6D4)   // Cyan
    )
    val goldenHour = listOf(
        Color(0xFFF59E0B),  // Amber
        Color(0xFFEF4444),  // Red-orange
        Color(0xFFF472B6)   // Pink
    )
    val emeraldWave = listOf(
        Color(0xFF10B981),  // Emerald
        Color(0xFF06B6D4),  // Cyan
        Color(0xFF3B82F6)   // Blue
    )
    val neonNight = listOf(
        Color(0xFFFF006E),  // Hot pink
        Color(0xFF8338EC),  // Purple
        Color(0xFF3A86FF)   // Electric blue
    )
}

// =============================================================================
// 3D Depth and Shadow Colors
// =============================================================================
val Depth3DLight = Color(0x40FFFFFF)      // 25% white for highlights
val Depth3DMedium = Color(0x1AFFFFFF)     // 10% white for mid layer
val Depth3DShadow = Color(0x33000000)     // 20% black for shadows
val Depth3DDeep = Color(0x4D000000)       // 30% black for deep shadows

// Liquid Glass Layer Colors
val LiquidGlassHighlight = Color(0xE6FFFFFF)  // 90% white top highlight
val LiquidGlassMid = Color(0x80FFFFFF)        // 50% white middle
val LiquidGlassBase = Color(0x33FFFFFF)       // 20% white base
val LiquidGlassBorder = Color(0x4DFFFFFF)     // 30% white border
val LiquidGlassShine = Color(0xCCFFFFFF)      // 80% white shine streak

// Dark mode liquid glass
val LiquidGlassDarkHighlight = Color(0x33FFFFFF)  // 20% white
val LiquidGlassDarkMid = Color(0x1AFFFFFF)        // 10% white
val LiquidGlassDarkBase = Color(0x0DFFFFFF)       // 5% white
val LiquidGlassDarkBorder = Color(0x26FFFFFF)     // 15% white

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
