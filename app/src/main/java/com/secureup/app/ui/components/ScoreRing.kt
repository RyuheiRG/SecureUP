package com.secureup.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secureup.app.ui.theme.Inter
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors

/**
 * Medidas leídas en Figma (A09a - Dashboard Home, Card 62:221 / anillo 62:223):
 * contenedor 160, banda 12, interior relleno con el token Background.
 */
private val RingSize = 160.dp
private val RingStrokeWidth = 12.dp

/**
 * "78": Inter Bold 48 con line-height 58 (Figma 62:227).
 * No existe un slot de tipografía con 48/58, por eso es un override del componente.
 */
private val ScoreNumberTextStyle = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Bold,
    fontSize = 48.sp,
    lineHeight = 58.sp,
)

/**
 * Anillo de puntuación de seguridad (Security Score).
 *
 * Un solo anillo continuo: el tramo coloreado es proporcional a [score] y el color cambia
 * según el rango (ver [ringColorFor]). Sustituye a los 4 arcos del diseño anterior.
 *
 * @param score puntuación actual; se limita al rango 0..[maxScore].
 */
@Composable
fun ScoreRing(
    score: Int,
    modifier: Modifier = Modifier,
    maxScore: Int = 100,
) {
    val safeMaxScore = maxScore.coerceAtLeast(1)
    val safeScore = score.coerceIn(0, safeMaxScore)
    val progress = safeScore.toFloat() / safeMaxScore
    val ringColor = ringColorFor(progress)
    val trackColor = SecureUpColors.SurfaceVariant

    Box(
        modifier = modifier
            .size(RingSize)
            .semantics(mergeDescendants = true) {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = safeScore.toFloat(),
                    range = 0f..safeMaxScore.toFloat(),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = RingStrokeWidth.toPx()
            val radius = (size.minDimension - strokePx) / 2f
            val arcSize = Size(radius * 2, radius * 2)
            val arcTopLeft = Offset(center.x - radius, center.y - radius)

            // Pista (parte vacía del anillo).
            drawCircle(color = trackColor, radius = radius, style = Stroke(strokePx))

            // Progreso: empieza arriba (-90°) y avanza en sentido horario.
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            // Interior oscuro: en Figma el centro usa el token Background.
            drawCircle(color = SecureUpColors.Background, radius = radius - strokePx / 2)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = safeScore.toString(),
                style = ScoreNumberTextStyle,
                color = ringColor,
            )
            Text(
                text = "/ $safeMaxScore",
                style = MaterialTheme.typography.bodySmall,
                color = SecureUpColors.TextSecondary,
            )
        }
    }
}

/**
 * Color del anillo según el avance (DESIGN.md, pendiente 1):
 *   >= 70 % -> Success (verde) | >= 40 % -> Warning (ámbar) | < 40 % -> Danger (rojo)
 * El puntaje se calcula solo con la fortaleza de las contraseñas guardadas.
 */
private fun ringColorFor(progress: Float): Color = when {
    progress >= 0.7f -> SecureUpColors.Success
    progress >= 0.4f -> SecureUpColors.Warning
    else -> SecureUpColors.Danger
}

@Preview(name = "ScoreRing - rangos", showBackground = true, backgroundColor = 0xFF222733)
@Composable
private fun ScoreRingPreview() {
    SecureUPTheme {
        Column {
            ScoreRing(score = 78)
            ScoreRing(score = 55)
            ScoreRing(score = 25)
        }
    }
}
