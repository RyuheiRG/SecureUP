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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

/** Variantes del botón. Fuente: Figma "Button" (nodos 4:102 a 4:118). */
enum class SecureUpButtonVariant { Primary, Secondary, Danger }

typealias ButtonStyle = SecureUpButtonVariant

private val ButtonVerticalPadding = 14.dp
private val ButtonBorderWidth = 1.dp

@Composable
fun SecureUpButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SecureUpButtonVariant = SecureUpButtonVariant.Primary,
    style: SecureUpButtonVariant = variant,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val effectiveVariant = if (style != SecureUpButtonVariant.Primary) style else variant
    val pressed by interactionSource.collectIsPressedAsState()

    val containerColor = when (effectiveVariant) {
        SecureUpButtonVariant.Primary -> SecureUpColors.Primary
        SecureUpButtonVariant.Secondary -> SecureUpColors.SurfaceVariant
        SecureUpButtonVariant.Danger -> SecureUpColors.Danger
    }
    val contentColor = if (effectiveVariant == SecureUpButtonVariant.Secondary) {
        SecureUpColors.TextPrimary
    } else {
        SecureUpColors.OnPrimary
    }
    val opacity = when {
        !enabled || isLoading -> 0.4f
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
                if (effectiveVariant == SecureUpButtonVariant.Secondary) {
                    Modifier.border(ButtonBorderWidth, SecureUpColors.Outline, shape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = Spacing.Xl, vertical = ButtonVerticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}
