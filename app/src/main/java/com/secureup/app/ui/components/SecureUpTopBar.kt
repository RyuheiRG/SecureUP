package com.secureup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.secureup.app.R
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

/**
 * Barra superior de SecureUP.
 * Fondo Surface, título alineado a la izquierda y flecha de volver opcional.
 */
@Composable
fun SecureUpTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SecureUpColors.Surface)
            .statusBarsPadding()
            .padding(horizontal = Spacing.L, vertical = Spacing.L),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = stringResource(id = R.string.back),
                    tint = SecureUpColors.TextPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
        } else {
            // Mantiene el título alineado con las pantallas que sí tienen flecha.
            Box(modifier = Modifier.size(40.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = SecureUpColors.TextPrimary,
            modifier = Modifier.padding(start = Spacing.S),
        )
    }
}

@Preview(name = "SecureUpTopBar", showBackground = true)
@Composable
private fun SecureUpTopBarPreview() {
    SecureUPTheme {
        SecureUpTopBar(title = "Mi Seguridad")
    }
}

@Preview(name = "SecureUpTopBar - con volver", showBackground = true)
@Composable
private fun SecureUpTopBarBackPreview() {
    SecureUPTheme {
        SecureUpTopBar(title = "Resultado", onBackClick = {})
    }
}
