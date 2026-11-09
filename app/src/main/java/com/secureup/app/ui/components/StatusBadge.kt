package com.secureup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

/** Variantes semánticas del badge de estado. */
enum class StatusBadgeVariant { Safe, Suspicious, Dangerous }

/**
 * Píldora con el veredicto de una URL o contraseña.
 * Fondo del color semántico al 12 % para que el texto conserve contraste.
 */
@Composable
fun StatusBadge(
    text: String,
    variant: StatusBadgeVariant,
    modifier: Modifier = Modifier,
) {
    val color = when (variant) {
        StatusBadgeVariant.Safe -> SecureUpColors.Success
        StatusBadgeVariant.Suspicious -> SecureUpColors.Warning
        StatusBadgeVariant.Dangerous -> SecureUpColors.Danger
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier
            .clip(RoundedCornerShape(Radius.Badge))
            .background(color.copy(alpha = BadgeBackgroundAlpha))
            .padding(horizontal = Spacing.M, vertical = Spacing.Xs),
    )
}

private const val BadgeBackgroundAlpha = 0.12f

@Preview(name = "StatusBadge", showBackground = true)
@Composable
private fun StatusBadgePreview() {
    SecureUPTheme {
        StatusBadge(text = "Seguro", variant = StatusBadgeVariant.Safe)
    }
}
