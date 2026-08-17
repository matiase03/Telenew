package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TvColorScheme = darkColorScheme(
  primary = TvPrimaryCyan,
  onPrimary = Color(0xFF00363D),
  primaryContainer = Color(0xFF004F58),
  onPrimaryContainer = Color(0xFF97F0FF),
  secondary = TvPrimaryBlue,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF004494),
  onSecondaryContainer = Color(0xFFD9E2FF),
  tertiary = TvAccentGold,
  onTertiary = Color(0xFF432C00),
  background = TvBackground,
  onBackground = TvTextPrimary,
  surface = TvSurface,
  onSurface = TvTextPrimary,
  surfaceVariant = TvSurfaceVariant,
  onSurfaceVariant = TvTextSecondary,
  error = TvError,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = TvColorScheme,
    typography = Typography,
    content = content
  )
}
