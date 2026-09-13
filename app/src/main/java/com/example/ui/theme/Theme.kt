package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val YalpsColorScheme = darkColorScheme(
  primary = YalpsPrimary,
  onPrimary = YalpsOnPrimary,
  primaryContainer = YalpsPrimaryContainer,
  onPrimaryContainer = YalpsOnPrimaryContainer,
  secondary = YalpsSecondary,
  onSecondary = YalpsOnSecondary,
  secondaryContainer = YalpsSecondaryContainer,
  onSecondaryContainer = YalpsOnSecondaryContainer,
  tertiary = YalpsTertiary,
  onTertiary = YalpsOnTertiary,
  tertiaryContainer = YalpsTertiaryContainer,
  background = YalpsBackground,
  onBackground = YalpsOnSurface,
  surface = YalpsSurface,
  onSurface = YalpsOnSurface,
  surfaceVariant = YalpsSurfaceContainerHigh,
  onSurfaceVariant = YalpsOnSurfaceVariant,
  outline = YalpsOutline,
  outlineVariant = YalpsOutlineVariant,
  error = YalpsError,
  errorContainer = YalpsErrorContainer,
  onErrorContainer = YalpsOnErrorContainer
)

@Composable
fun YalpsTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = YalpsColorScheme,
    typography = Typography,
    content = content
  )
}

