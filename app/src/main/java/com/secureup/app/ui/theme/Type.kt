package com.secureup.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.secureup.app.R

/**
 * Tipografía Inter.
 * REQUISITO: los archivos inter_regular.ttf, inter_medium.ttf, inter_semibold.ttf e
 * inter_bold.ttf deben existir en app/src/main/res/font/ (nombres en minúsculas, sin guiones).
 */
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

/**
 * Escala definida en Figma (Design System -> Typography, nodo 4:72):
 *   Display 28 / Bold      -> headlineLarge (line-height 36, letter-spacing -0.5)
 *   Title 20 / Semi Bold   -> titleLarge    (line-height 28, letter-spacing -0.2)
 *   Body 16 / Regular      -> bodyLarge     (line-height 24, letter-spacing 0)
 *   Caption 12 / Regular   -> bodySmall     (line-height 16, letter-spacing 0.2)
 * labelLarge: override visto en botones y campos del login (16 / line-height 20).
 * labelMedium: etiqueta de los campos de texto (Medium 12 / line-height 16).
 * El peso SemiBold de labelLarge sigue POR CONFIRMAR.
 *
 * SLOTS SIN DISEÑO: `Typography()` solo define 6 slots. El resto (display*, headlineMedium/Small,
 * titleMedium/Small, bodyMedium, labelSmall) se toman de la escala de Material 3 y solo se les
 * cambia la familia a Inter. Así ningún componente de Material (Snackbar, AlertDialog, menús…)
 * cae en la fuente por defecto del sistema (Roboto).
 */
private val MaterialDefaultTypography = Typography()

private fun TextStyle.withInterFamily(): TextStyle = copy(fontFamily = Inter)

val SecureUpTypography = Typography(
    // --- Sin diseño en Figma: escala de Material con Inter ---
    displayLarge = MaterialDefaultTypography.displayLarge.withInterFamily(),
    displayMedium = MaterialDefaultTypography.displayMedium.withInterFamily(),
    displaySmall = MaterialDefaultTypography.displaySmall.withInterFamily(),
    headlineMedium = MaterialDefaultTypography.headlineMedium.withInterFamily(),
    headlineSmall = MaterialDefaultTypography.headlineSmall.withInterFamily(),
    titleMedium = MaterialDefaultTypography.titleMedium.withInterFamily(),
    titleSmall = MaterialDefaultTypography.titleSmall.withInterFamily(),
    bodyMedium = MaterialDefaultTypography.bodyMedium.withInterFamily(),
    labelSmall = MaterialDefaultTypography.labelSmall.withInterFamily(),

    // --- Escala confirmada en Figma (nodo 4:72) ---
    headlineLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
    ),
    labelLarge = TextStyle( // texto de botones y campos del login (override)
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold, // POR CONFIRMAR en Figma
        fontSize = 16.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle( // etiqueta arriba de los campos de texto
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)

/**
 * Valor de las tarjetas de estadísticas del Inicio (Contraseñas, Brechas).
 * Figma (nodos 62:232 y 62:234): Inter SemiBold 16 / line-height 24.
 * No se usa `titleLarge` (20/28) porque el diseño baja a 16 para caber en la tarjeta.
 */
val StatValueTextStyle = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)

/**
 * Texto de los accesos rápidos del Inicio (botones "+ Nueva entrada" y "Escanear QR").
 * Figma (nodos 62:240 y 62:243): Inter Medium 14 con line-height automático (`normal`),
 * por eso no se fija `lineHeight`: Compose usa las métricas de la fuente.
 */
val ActionLabelTextStyle = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
)

/**
 * Texto dentro de los campos de entrada.
 * Figma (login, nodo 87:216): 16 / line-height 20 Regular, un override del Body 16/24.
 * Se separa de `bodyLarge` para respetar el alto exacto del campo (padding 14 + línea 20).
 */
val FieldTextStyle = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 20.sp,
)
