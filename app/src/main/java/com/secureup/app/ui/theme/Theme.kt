package com.secureup.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Esquema de color. Todos los roles de Material 3 se mapean a los tokens de DESIGN.md
 * para que ningún componente (diálogos, snackbars, menús) caiga en los colores
 * por defecto del baseline de Material (que son violetas).
 */
private val SecureUpColorScheme = darkColorScheme(
    primary = SecureUpColors.Primary,
    onPrimary = SecureUpColors.OnPrimary,
    primaryContainer = SecureUpColors.SurfaceVariant,
    onPrimaryContainer = SecureUpColors.TextPrimary,
    inversePrimary = SecureUpColors.Primary,

    secondary = SecureUpColors.TextSecondary,
    onSecondary = SecureUpColors.OnPrimary,
    secondaryContainer = SecureUpColors.SurfaceVariant,
    onSecondaryContainer = SecureUpColors.TextPrimary,

    tertiary = SecureUpColors.Primary,
    onTertiary = SecureUpColors.OnPrimary,
    tertiaryContainer = SecureUpColors.SurfaceVariant,
    onTertiaryContainer = SecureUpColors.TextPrimary,

    background = SecureUpColors.Background,
    onBackground = SecureUpColors.TextPrimary,

    surface = SecureUpColors.Surface,
    onSurface = SecureUpColors.TextPrimary,
    surfaceVariant = SecureUpColors.SurfaceVariant,
    onSurfaceVariant = SecureUpColors.TextSecondary,
    surfaceTint = SecureUpColors.Primary,

    // Superficies con elevación de Material 3 (AlertDialog usa surfaceContainerHigh).
    surfaceDim = SecureUpColors.Background,
    surfaceBright = SecureUpColors.SurfaceVariant,
    surfaceContainerLowest = SecureUpColors.Background,
    surfaceContainerLow = SecureUpColors.Surface,
    surfaceContainer = SecureUpColors.Surface,
    surfaceContainerHigh = SecureUpColors.SurfaceVariant,
    surfaceContainerHighest = SecureUpColors.SurfaceVariant,

    inverseSurface = SecureUpColors.SurfaceVariant,
    inverseOnSurface = SecureUpColors.TextPrimary,

    error = SecureUpColors.Danger,
    onError = SecureUpColors.OnPrimary,
    errorContainer = SecureUpColors.Danger,
    onErrorContainer = SecureUpColors.OnPrimary,

    outline = SecureUpColors.Outline,
    outlineVariant = SecureUpColors.Outline,
)

/**
 * Formas globales. Los valores salen de `Radius` (12 controles / 16 tarjetas).
 * `extraLarge` = 16 porque Material usa ese slot para `AlertDialog`
 * (AlertDialogDefaults.shape = shapes.extraLarge): así los diálogos respetan el diseño.
 */
private val SecureUpShapes = Shapes(
    extraSmall = RoundedCornerShape(Radius.Control),
    small = RoundedCornerShape(Radius.Control),
    medium = RoundedCornerShape(Radius.Control),
    large = RoundedCornerShape(Radius.Card),
    extraLarge = RoundedCornerShape(Radius.Card),
)

/** La app es solo tema oscuro (así está diseñada). No usa color dinámico ni sigue el tema del sistema. */
@Composable
fun SecureUPTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SecureUpColorScheme,
        typography = SecureUpTypography,
        shapes = SecureUpShapes,
        content = content,
    )
}
