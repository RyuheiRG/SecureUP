package com.secureup.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Tokens de forma (radios de esquina) de SecureUP.
 * Fuente: Figma -> Design System (componentes Button y TextField).
 * Regla: ningún dp de radio "suelto" en pantallas; todo sale de aquí.
 */
object Radius {
    /** Botones y campos de texto. Confirmado en Figma: 12 dp. */
    val Control = 12.dp

    /** Tarjetas y avisos. Confirmado en Figma: 16 dp (aviso Danger, nodo 87:197). */
    val Card = 16.dp

    /** Badges en forma de píldora (extremos semicirculares). */
    val Badge = 999.dp
}

/**
 * Escala de espaciado de SecureUP. Confirmada en Figma.
 * Uso típico: Xs entre elementos muy juntos, L como padding de pantalla.
 */
object Spacing {
    val Xs = 4.dp
    val S = 8.dp
    val M = 12.dp
    val L = 16.dp
    val Xl = 24.dp
    val Xxl = 32.dp
}
