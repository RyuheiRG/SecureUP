package com.secureup.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SecureUpColorScheme = darkColorScheme(
    primary = SecureUpColors.Primary,
    onPrimary = SecureUpColors.OnPrimary,
    background = SecureUpColors.Background,
    onBackground = SecureUpColors.TextPrimary,
    surface = SecureUpColors.Surface,
    onSurface = SecureUpColors.TextPrimary,
    surfaceVariant = SecureUpColors.SurfaceVariant,
    onSurfaceVariant = SecureUpColors.TextSecondary,
    error = SecureUpColors.Danger,
    onError = SecureUpColors.OnPrimary,
    outline = SecureUpColors.Outline,
)

/** La app es solo tema oscuro (así está diseñada). No usa color dinámico ni sigue el tema del sistema. */
@Composable
fun SecureUPTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SecureUpColorScheme,
        typography = SecureUpTypography,
        content = content,
    )
}