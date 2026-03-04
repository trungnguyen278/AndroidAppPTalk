package com.avis.app.ptalk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * App Theme Colors - Supports both Dark and Light mode
 * Primary colors: Red (PTIT brand) with Orange/Yellow accents
 */
object TechColors {
    // PTIT Brand Colors
    val PTITRed = Color(0xFFE31B23)
    val PTITRedDark = Color(0xFFB71C1C)
    val OrangeAccent = Color(0xFFFF9800)
    val YellowStar = Color(0xFFFFD700)
    
    // Primary gradient colors
    val CyanPrimary = Color(0xFF00F5FF)
    val CyanDark = Color(0xFF00CED1)
    val TealAccent = Color(0xFF00E5CC)
    
    // Secondary - Purple
    val PurpleAccent = Color(0xFF9D4EDD)
    val PurpleGlow = Color(0xFFBB86FC)
    
    // Dark theme colors
    val DarkBackground = Color(0xFF0A0E14)
    val DarkSurface = Color(0xFF121820)
    val DarkCard = Color(0xFF1A2332)
    val DarkCardHighlight = Color(0xFF243044)
    
    // Light theme colors
    val LightBackground = Color(0xFFF5F7FA)
    val LightSurface = Color(0xFFFFFFFF)
    val LightCard = Color(0xFFFFFFFF)
    val LightCardHighlight = Color(0xFFF0F4F8)
    
    // Glow effects
    val CyanGlow = Color(0x4000F5FF)
    val PurpleGlowAlpha = Color(0x409D4EDD)
    val RedGlow = Color(0x40E31B23)
    
    // Status colors
    val SuccessGreen = Color(0xFF00C853)
    val WarningOrange = Color(0xFFFF9800)
    val ErrorRed = Color(0xFFE31B23)
    
    // Text colors - Dark theme
    val TextPrimaryDark = Color(0xFFE8EAED)
    val TextSecondaryDark = Color(0xFFB0B8C4)
    val TextMutedDark = Color(0xFF6B7280)
    
    // Text colors - Light theme
    val TextPrimaryLight = Color(0xFF1A1A1A)
    val TextSecondaryLight = Color(0xFF4A5568)
    val TextMutedLight = Color(0xFF9CA3AF)
}

/**
 * Dynamic theme colors based on system preference
 */
data class AppColors(
    val background: Color,
    val surface: Color,
    val card: Color,
    val cardHighlight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primary: Color,
    val accent: Color,
    val glow: Color,
    val isDark: Boolean
)

@Composable
fun appColors(darkTheme: Boolean = isSystemInDarkTheme()): AppColors {
    return if (darkTheme) {
        AppColors(
            background = TechColors.DarkBackground,
            surface = TechColors.DarkSurface,
            card = TechColors.DarkCard,
            cardHighlight = TechColors.DarkCardHighlight,
            textPrimary = TechColors.TextPrimaryDark,
            textSecondary = TechColors.TextSecondaryDark,
            textMuted = TechColors.TextMutedDark,
            primary = TechColors.PTITRed,
            accent = TechColors.OrangeAccent,
            glow = TechColors.RedGlow,
            isDark = true
        )
    } else {
        AppColors(
            background = TechColors.LightBackground,
            surface = TechColors.LightSurface,
            card = TechColors.LightCard,
            cardHighlight = TechColors.LightCardHighlight,
            textPrimary = TechColors.TextPrimaryLight,
            textSecondary = TechColors.TextSecondaryLight,
            textMuted = TechColors.TextMutedLight,
            primary = TechColors.PTITRed,
            accent = TechColors.OrangeAccent,
            glow = TechColors.RedGlow,
            isDark = false
        )
    }
}
