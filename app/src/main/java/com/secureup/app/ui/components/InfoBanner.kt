package com.secureup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secureup.app.R
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

/** Variantes del aviso. Fuente: Figma `87:197` (Danger) y `96:278` (Warning). */
enum class InfoBannerVariant { Warning, Danger }

/**
 * Métricas leídas en Figma.
 * - Danger: padding 14, radio 16 (Radius.Card), borde 1, texto 13/18.
 * - Warning: padding 12 horizontal / 6 arriba / 12 abajo, radio 8, texto 12/16.
 */
private val BannerIconBoxSize = 20.dp
private val BannerIconSize = 18.dp
private val BannerIconGap = 12.dp
private val BannerBorderWidth = 1.dp
private val WarningCornerRadius = 8.dp
private val WarningHorizontalPadding = 12.dp
private val WarningTopPadding = 6.dp
private val WarningBottomPadding = 12.dp
private val DangerPadding = 14.dp

/** El aviso Danger de Figma usa 13/18: no existe un slot con esas medidas en la escala. */
private val DangerTextStyle = TextStyle(
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.sp,
)

/**
 * Aviso informativo con ícono de alerta.
 *
 * El ícono siempre es `ic_alert_triangle`; solo cambia el color (tint) y el contenedor
 * según la variante. El texto lo aporta quien lo usa (ver `strings.xml`).
 */
@Composable
fun InfoBanner(
    text: String,
    modifier: Modifier = Modifier,
    variant: InfoBannerVariant = InfoBannerVariant.Warning,
) {
    val accentColor = when (variant) {
        InfoBannerVariant.Warning -> SecureUpColors.Warning
        InfoBannerVariant.Danger -> SecureUpColors.Danger
    }
    val containerColor = when (variant) {
        InfoBannerVariant.Warning -> SecureUpColors.Surface
        InfoBannerVariant.Danger -> SecureUpColors.Danger.copy(alpha = DangerBackgroundAlpha)
    }
    val shape = RoundedCornerShape(
        when (variant) {
            InfoBannerVariant.Warning -> WarningCornerRadius
            InfoBannerVariant.Danger -> Radius.Card
        }
    )
    val contentPadding = when (variant) {
        InfoBannerVariant.Warning -> PaddingValues(
            start = WarningHorizontalPadding,
            top = WarningTopPadding,
            end = WarningHorizontalPadding,
            bottom = WarningBottomPadding,
        )
        InfoBannerVariant.Danger -> PaddingValues(DangerPadding)
    }
    val textStyle = when (variant) {
        InfoBannerVariant.Warning -> MaterialTheme.typography.bodySmall
        InfoBannerVariant.Danger -> MaterialTheme.typography.bodySmall.merge(DangerTextStyle)
    }
    val textColor = when (variant) {
        InfoBannerVariant.Warning -> SecureUpColors.TextSecondary
        InfoBannerVariant.Danger -> SecureUpColors.Danger
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor, shape)
            .then(
                if (variant == InfoBannerVariant.Danger) {
                    Modifier.border(BannerBorderWidth, SecureUpColors.Danger, shape)
                } else {
                    Modifier
                }
            )
            .padding(contentPadding)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(BannerIconGap),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(BannerIconBoxSize),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_alert_triangle),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(BannerIconSize),
            )
        }
        Text(
            text = text,
            style = textStyle,
            color = textColor,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Figma `87:197`: relleno Danger con 8 % de opacidad. */
private const val DangerBackgroundAlpha = 0.08f

@Preview(name = "InfoBanner - Warning y Danger", showBackground = true)
@Composable
private fun InfoBannerPreview() {
    SecureUPTheme {
        Column(
            modifier = Modifier
                .background(SecureUpColors.Background)
                .padding(Spacing.L),
            verticalArrangement = Arrangement.spacedBy(Spacing.Xl),
        ) {
            InfoBanner(
                text = "Esto puede tardar unos segundos mientras se actualiza tu bóveda.",
                variant = InfoBannerVariant.Warning,
            )
            InfoBanner(
                text = "Si olvidas tu contraseña maestra, no podrás recuperar tu bóveda. " +
                    "Guárdala en un lugar seguro.",
                variant = InfoBannerVariant.Danger,
            )
        }
    }
}
