package com.secureup.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

enum class ButtonStyle {
    Primary,
    Secondary,
    Danger,
}

/**
 * Botón principal reutilizable de SecureUP.
 * Soporta variantes Primary (azul), Secondary (borde) y Danger (rojo), así como indicador de carga.
 */
@Composable
fun SecureUpButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val containerColor = when (style) {
        ButtonStyle.Primary -> SecureUpColors.Primary
        ButtonStyle.Secondary -> SecureUpColors.SurfaceVariant
        ButtonStyle.Danger -> SecureUpColors.Danger
    }

    val contentColor = when (style) {
        ButtonStyle.Primary -> SecureUpColors.OnPrimary
        ButtonStyle.Secondary -> SecureUpColors.TextPrimary
        ButtonStyle.Danger -> SecureUpColors.OnPrimary
    }

    val border = if (style == ButtonStyle.Secondary) {
        BorderStroke(1.dp, SecureUpColors.Outline)
    } else null

    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(Radius.Control),
        border = border,
        contentPadding = PaddingValues(horizontal = Spacing.L, vertical = Spacing.M),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.4f),
        ),
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
            )
        }
    }
}
