package com.secureup.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Tokens de color de SecureUP.
 * Fuente: Figma -> página "Design System" -> "Color Palette".
 * Regla: ningún valor hexadecimal fuera de este archivo.
 */
object SecureUpColors {
    val Background = Color(0xFF0F1115)
    val Surface = Color(0xFF181B22)
    val SurfaceVariant = Color(0xFF222733)
    val Primary = Color(0xFF4F8CFF)
    val OnPrimary = Color(0xFF0A1020)
    val Success = Color(0xFF3DDC97)
    val Warning = Color(0xFFFFB84D)
    val Danger = Color(0xFFFF5C6C)
    val TextPrimary = Color(0xFFE8EAF0)
    val TextSecondary = Color(0xFF9AA3B5)
    // Tarjeta de brechas del Inicio (Figma 62:233). Son colores sólidos del diseño,
    // no Danger con opacidad: así el valor se lee igual sobre cualquier fondo.
    val DangerSurface = Color(0xFF2A1418)
    val DangerMuted = Color(0xFFC47580)
    // Borde del botón secundario. En Figma es un hex literal (no está ligado a una variable).
    val Outline = Color(0xFF4D5973)
}