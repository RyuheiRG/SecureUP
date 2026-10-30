package com.secureup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing
import com.secureup.app.ui.theme.StatValueTextStyle

/**
 * Variantes de la tarjeta de estadística. Fuente: Figma `62:230` (Neutral) y `62:233` (Danger).
 */
enum class StatCardVariant { Neutral, Danger }

/**
 * Tarjeta con una etiqueta arriba y un valor abajo (Contraseñas guardadas / Brechas detectadas).
 * Métricas leídas en Figma: radio 16 (`Radius.Card`), padding 16, separación etiqueta-valor 8.
 * Los colores cambian por variante:
 *  - Neutral: fondo `SurfaceVariant`, valor `Primary`.
 *  - Danger: fondo `DangerSurface`, etiqueta `DangerMuted`, valor `Danger`.
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    variant: StatCardVariant = StatCardVariant.Neutral,
) {
    val surfaceColor: Color
    val labelColor: Color
    val valueColor: Color
    when (variant) {
        StatCardVariant.Neutral -> {
            surfaceColor = SecureUpColors.SurfaceVariant
            labelColor = SecureUpColors.TextSecondary
            valueColor = SecureUpColors.Primary
        }

        StatCardVariant.Danger -> {
            surfaceColor = SecureUpColors.DangerSurface
            labelColor = SecureUpColors.DangerMuted
            valueColor = SecureUpColors.Danger
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.Card))
            .background(surfaceColor)
            .padding(Spacing.L),
        verticalArrangement = Arrangement.spacedBy(Spacing.S),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = labelColor,
        )
        Text(
            text = value,
            style = StatValueTextStyle,
            color = valueColor,
        )
    }
}

@Preview(name = "StatCard - variantes", showBackground = true)
@Composable
private fun StatCardPreview() {
    SecureUPTheme {
        Column(
            modifier = Modifier.padding(Spacing.L),
            verticalArrangement = Arrangement.spacedBy(Spacing.M),
        ) {
            StatCard(label = "Contraseñas", value = "12 Guardadas")
            StatCard(label = "Brechas", value = "2 Detectadas", variant = StatCardVariant.Danger)
        }
    }
}
