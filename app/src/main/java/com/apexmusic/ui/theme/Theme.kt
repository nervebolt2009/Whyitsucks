package com.apexmusic.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography

// ── ApexMusic AMOLED Color Palette ───────────────────────────────────────────
// Pure black background = maximum OLED battery saving on GW7 Super AMOLED display

val ApexBlack = Color(0xFF000000)          // Pure AMOLED black background
val ApexDarkSurface = Color(0xFF0D0D0D)    // Slightly lighter surface
val ApexCardBg = Color(0xFF141414)         // Card backgrounds
val ApexAccent = Color(0xFF7C4DFF)         // Purple accent — vibrant on AMOLED
val ApexAccentLight = Color(0xFFB47CFF)    // Light purple for secondary elements
val ApexWhite = Color(0xFFFFFFFF)          // Primary text
val ApexGrey = Color(0xFF9E9E9E)           // Secondary text
val ApexDivider = Color(0xFF1E1E1E)        // Dividers
val ApexError = Color(0xFFCF6679)          // Error states

val ApexColorScheme = ColorScheme(
    primary = ApexAccent,
    primaryDim = ApexAccentLight,
    primaryContainer = Color(0xFF3700B3),
    onPrimary = ApexWhite,
    onPrimaryContainer = ApexWhite,
    secondary = ApexAccentLight,
    secondaryDim = Color(0xFF9965D6),
    secondaryContainer = Color(0xFF1E0066),
    onSecondary = ApexBlack,
    onSecondaryContainer = ApexWhite,
    tertiary = Color(0xFF03DAC6),
    tertiaryDim = Color(0xFF03B9A8),
    tertiaryContainer = Color(0xFF003733),
    onTertiary = ApexBlack,
    onTertiaryContainer = ApexWhite,
    surface = ApexDarkSurface,
    onSurface = ApexWhite,
    onSurfaceVariant = ApexGrey,
    background = ApexBlack,
    error = ApexError,
    onError = ApexBlack,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = ApexDivider,
    outlineVariant = Color(0xFF262626)
)

@Composable
fun ApexMusicTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ApexColorScheme,
        content = content
    )
}
