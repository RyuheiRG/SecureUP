package com.secureup.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.secureup.app.R
import com.secureup.app.ui.components.BottomBarDestination
import com.secureup.app.ui.components.ScoreRing
import com.secureup.app.ui.components.SecureUpBottomBar
import com.secureup.app.ui.components.SecureUpTopBar
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
                .padding(horizontal = Spacing.Xl, vertical = Spacing.L),
            verticalArrangement = Arrangement.spacedBy(Spacing.L),
        ) {
            // Tarjeta principal: Security Score
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.Card))
                    .background(SecureUpColors.SurfaceVariant)
                    .padding(Spacing.L),
            ) {
                Text(
                    text = stringResource(id = R.string.home_score_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecureUpColors.TextSecondary,
                )

                Spacer(modifier = Modifier.height(Spacing.M))

                ScoreRing(
                    score = 78,
                    maxScore = 100,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            // Tarjeta secundaria: Contraseñas guardadas
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.Card))
                    .background(SecureUpColors.SurfaceVariant)
                    .padding(horizontal = Spacing.L, vertical = Spacing.L),
            ) {
                Text(
                    text = stringResource(id = R.string.home_passwords_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecureUpColors.TextSecondary,
                )

                Spacer(modifier = Modifier.height(Spacing.Xs))

                Text(
                    text = stringResource(id = R.string.home_passwords_value),
                    style = MaterialTheme.typography.titleLarge,
                    color = SecureUpColors.Primary,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Tarjeta secundaria: Brechas detectadas
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.Card))
                    .background(SecureUpColors.Danger.copy(alpha = 0.15f))
                    .padding(horizontal = Spacing.L, vertical = Spacing.L),
            ) {
                Text(
                    text = stringResource(id = R.string.home_breaches_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecureUpColors.TextSecondary,
                )

                Spacer(modifier = Modifier.height(Spacing.Xs))

                Text(
                    text = stringResource(id = R.string.home_breaches_value),
                    style = MaterialTheme.typography.titleLarge,
                    color = SecureUpColors.Danger,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.S))

            // Sección: Accesos rápidos
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
                    // Botón de acceso rápido: + Nueva entrada
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Radius.Control))
                            .background(SecureUpColors.SurfaceVariant)
                            .clickable { onNewEntryClick() }
                            .padding(vertical = Spacing.L, horizontal = Spacing.M),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(id = R.string.home_new_entry_button),
                            style = MaterialTheme.typography.labelLarge,
                            color = SecureUpColors.Primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    // Botón de acceso rápido: Escanear QR
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Radius.Control))
                            .background(SecureUpColors.SurfaceVariant)
                            .clickable { onScanQrClick() }
                            .padding(vertical = Spacing.L, horizontal = Spacing.M),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_qr_code),
                            contentDescription = null,
                            tint = SecureUpColors.Primary,
                            modifier = Modifier.size(18.dp),
                        )

                        Spacer(modifier = Modifier.width(Spacing.S))

                        Text(
                            text = stringResource(id = R.string.home_scan_qr_button),
                            style = MaterialTheme.typography.labelLarge,
                            color = SecureUpColors.Primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
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
