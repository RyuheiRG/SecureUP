package com.secureup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

/**
 * Barra superior de SecureUP.
 * Fondo Surface, título alineado a la izquierda.
 */
@Composable
fun SecureUpTopBar(
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SecureUpColors.Surface)
            .statusBarsPadding()
            .padding(horizontal = Spacing.L, vertical = Spacing.L),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = SecureUpColors.TextPrimary,
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
