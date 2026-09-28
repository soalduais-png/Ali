package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = SaffronPrimaryDark,
    onPrimary = Color(0xFF3E1000),
    primaryContainer = Color(0xFF7A2106),
    onPrimaryContainer = SaffronContainer,
    secondary = GoldSecondary,
    onSecondary = Color(0xFF3E2723),
    secondaryContainer = Color(0xFF5D4037),
    onSecondaryContainer = GoldSecondaryContainer,
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF064E3B),
    tertiaryContainer = Color(0xFF065F46),
    onTertiaryContainer = EmeraldContainer,
    background = WarmBackgroundDark,
    onBackground = Color(0xFFF5F5F4),
    surface = WarmSurfaceDark,
    onSurface = Color(0xFFF5F5F4),
    surfaceVariant = WarmSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFD6D3D1)
)

private val LightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = Color.White,
    primaryContainer = SaffronContainer,
    onPrimaryContainer = OnSaffronContainer,
    secondary = GoldSecondary,
    onSecondary = Color.White,
    secondaryContainer = GoldSecondaryContainer,
    onSecondaryContainer = OnGoldContainer,
    tertiary = EmeraldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = OnEmeraldContainer,
    background = WarmBackgroundLight,
    onBackground = Color(0xFF1C1917),
    surface = WarmSurfaceLight,
    onSurface = Color(0xFF1C1917),
    surfaceVariant = WarmSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF57534E)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
