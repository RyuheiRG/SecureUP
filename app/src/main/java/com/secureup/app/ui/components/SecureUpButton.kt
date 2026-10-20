package com.secureup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

/** Variantes del botón. Fuente: Figma "Button" (nodos 4:102 a 4:118). */
enum class SecureUpButtonVariant { Primary, Secondary, Danger }

/** Padding vertical del botón (Figma: 14 px). No está en la escala Spacing. */
private val ButtonVerticalPadding = 14.dp

/** Grosor del borde del botón secundario. */
private val ButtonBorderWidth = 1.dp

/**
 * Botón principal de SecureUP.
 *
 * Colores, radio y estados confirmados en Figma (nodo "Button" 4:120):
 *  - Primary y Danger: relleno con el token del color y texto OnPrimary.
 *  - Secondary: relleno SurfaceVariant, borde Outline y texto TextPrimary.
 *  - Estados por opacidad: normal 100 %, pressed 80 %, disabled 40 %.
 *
 * @param interactionSource permite simular el estado "pressed" en los @Preview.
 */
@Composable
fun SecureUpButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SecureUpButtonVariant = SecureUpButtonVariant.Primary,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val pressed by interactionSource.collectIsPressedAsState()

    val containerColor = when (variant) {
        SecureUpButtonVariant.Primary -> SecureUpColors.Primary
        SecureUpButtonVariant.Secondary -> SecureUpColors.SurfaceVariant
        SecureUpButtonVariant.Danger -> SecureUpColors.Danger
    }
    val contentColor = if (variant == SecureUpButtonVariant.Secondary) {
        SecureUpColors.TextPrimary
    } else {
        SecureUpColors.OnPrimary
    }
    val opacity = when {
        !enabled -> 0.4f
        pressed -> 0.8f
        else -> 1f
    }
    val shape = RoundedCornerShape(Radius.Control)

    Box(
        modifier = modifier
            .alpha(opacity)
            .clip(shape)
            .background(containerColor, shape)
            .then(
                if (variant == SecureUpButtonVariant.Secondary) {
                    Modifier.border(ButtonBorderWidth, SecureUpColors.Outline, shape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // el feedback de "pressed" es la opacidad, no un ripple
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = Spacing.Xl, vertical = ButtonVerticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "SecureUpButton - todos los estados")
@Composable
private fun SecureUpButtonPreview() {
    SecureUPTheme {
        Column(
            modifier = Modifier
                .background(SecureUpColors.Background)
                .padding(Spacing.L),
            verticalArrangement = Arrangement.spacedBy(Spacing.M),
        ) {
            SecureUpButtonVariant.entries.forEach { variant ->
                Text(
                    text = variant.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = SecureUpColors.TextSecondary,
                )
                SecureUpButton(
                    text = "Normal",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    variant = variant,
                )
                SecureUpPressedButtonPreview(variant)
                SecureUpButton(
                    text = "Deshabilitado",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    variant = variant,
                    enabled = false,
                )
            }
        }
    }
}

/** Muestra el estado "pressed" emitiendo una interacción de presión. */
@Composable
private fun SecureUpPressedButtonPreview(variant: SecureUpButtonVariant) {
    val source = remember { MutableInteractionSource() }
    LaunchedEffect(source) { source.emit(PressInteraction.Press(Offset.Zero)) }
    SecureUpButton(
        text = "Pressed",
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        variant = variant,
        interactionSource = source,
    )
}
