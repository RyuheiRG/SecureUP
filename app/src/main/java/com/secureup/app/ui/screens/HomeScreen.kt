package com.secureup.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.secureup.app.R
import com.secureup.app.ui.components.BottomBarDestination
import com.secureup.app.ui.components.QuickActionButton
import com.secureup.app.ui.components.ScoreRing
import com.secureup.app.ui.components.SecureUpBottomBar
import com.secureup.app.ui.components.SecureUpTopBar
import com.secureup.app.ui.components.StatCard
import com.secureup.app.ui.components.StatCardVariant
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

/**
 * Pantalla de Inicio / Dashboard (Frame A09a - Mi Seguridad).
 * Muestra el Security Score, contadores de contraseñas y brechas,
 * accesos rápidos y navegación inferior.
 */
@Composable
fun HomeScreen(
    onNewEntryClick: () -> Unit = {},
    onScanQrClick: () -> Unit = {},
    onDestinationSelected: (BottomBarDestination) -> Unit = {},
) {
    Scaffold(
        topBar = {
            SecureUpTopBar(title = stringResource(id = R.string.home_title))
        },
        bottomBar = {
            SecureUpBottomBar(
                currentDestination = BottomBarDestination.Home,
                onDestinationSelected = onDestinationSelected,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.L, vertical = Spacing.L),
            verticalArrangement = Arrangement.spacedBy(Spacing.L),
        ) {
            // Tarjeta principal: Security Score
            // Figma (62:221): padding 24 horizontal; 40 vertical ajustado a Spacing.Xxl (32);
            // separación de 20 entre la etiqueta y el anillo ajustada a Spacing.Xl (24).
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.Card))
                    .background(SecureUpColors.SurfaceVariant)
                    .padding(horizontal = Spacing.Xl, vertical = Spacing.Xxl),
            ) {
                Text(
                    text = stringResource(id = R.string.home_score_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecureUpColors.TextSecondary,
                )

                Spacer(modifier = Modifier.height(Spacing.Xl))

                ScoreRing(
                    score = 78,
                    maxScore = 100,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            // Tarjetas de estadísticas: en Figma (`62:229`) van juntas con 12 dp entre ellas
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
                // Contraseñas guardadas
                StatCard(
                    label = stringResource(id = R.string.home_passwords_label),
                    value = stringResource(id = R.string.home_passwords_value),
                )

                // Brechas detectadas
                StatCard(
                    label = stringResource(id = R.string.home_breaches_label),
                    value = stringResource(id = R.string.home_breaches_value),
                    variant = StatCardVariant.Danger,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.S))

            // Sección: Accesos rápidos (Figma 62:238)
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
                Text(
                    text = stringResource(id = R.string.home_quick_access_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecureUpColors.TextSecondary,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.M),
                ) {
                    QuickActionButton(
                        text = stringResource(id = R.string.home_new_entry_button),
                        onClick = onNewEntryClick,
                        modifier = Modifier.weight(1f),
                    )

                    QuickActionButton(
                        text = stringResource(id = R.string.home_scan_qr_button),
                        onClick = onScanQrClick,
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.ic_qr_code,
                    )
                }
            }
        }
    }
}

@Preview(name = "HomeScreen", showBackground = true)
@Composable
private fun HomeScreenPreview() {
    SecureUPTheme {
        HomeScreen()
    }
}
