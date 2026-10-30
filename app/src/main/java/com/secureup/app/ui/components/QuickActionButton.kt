package com.secureup.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.secureup.app.R
import com.secureup.app.ui.theme.ActionLabelTextStyle
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

/** Métricas leídas en Figma (`62:239` y `62:241`): alto 64, ícono 14, separación ícono-texto 6. */
private val QuickActionMinHeight = 64.dp
private val QuickActionIconSize = 14.dp
private val QuickActionIconGap = 6.dp

/**
 * Acceso rápido del Inicio: "+ Nueva entrada" y "Escanear QR".
 * Fondo `SurfaceVariant`, radio 12 (`Radius.Control`), texto Inter Medium 14 en `Primary`.
 * El ícono es opcional y decorativo: `contentDescription = null` porque el texto de al lado ya nombra la acción.
 */
@Composable
fun QuickActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
) {
    Row(
        modifier = modifier
            .heightIn(min = QuickActionMinHeight)
            .clip(RoundedCornerShape(Radius.Control))
            .background(SecureUpColors.SurfaceVariant)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.L, vertical = Spacing.M),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = SecureUpColors.Primary,
                modifier = Modifier.size(QuickActionIconSize),
            )
            Spacer(modifier = Modifier.width(QuickActionIconGap))
        }
        Text(
            text = text,
            style = ActionLabelTextStyle,
            color = SecureUpColors.Primary,
        )
    }
}

@Preview(name = "QuickActionButton", showBackground = true)
@Composable
private fun QuickActionButtonPreview() {
    SecureUPTheme {
        Row(
            modifier = Modifier.padding(Spacing.L),
            horizontalArrangement = Arrangement.spacedBy(Spacing.M),
        ) {
            QuickActionButton(text = "Nueva entrada", onClick = {})
            QuickActionButton(
                text = "Escanear QR",
                onClick = {},
                iconRes = R.drawable.ic_qr_code,
            )
        }
    }
}
