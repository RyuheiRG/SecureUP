package com.secureup.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.secureup.app.R
import com.secureup.app.ui.components.BottomBarDestination
import com.secureup.app.ui.components.SecureUpBottomBar
import com.secureup.app.ui.components.SecureUpButton
import com.secureup.app.ui.components.SecureUpButtonVariant
import com.secureup.app.ui.components.SecureUpTopBar
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing
import com.secureup.app.viewmodel.ProfileViewModel

/**
 * Perfil (Frame "perfil"). Muestra la cuenta de la sesión y permite cerrarla.
 *
 * Cambiar la contraseña maestra requiere la bóveda y el backend; se pospone a
 * la entrega 26.3.1 y se declara en la tarjeta de "Próximamente".
 */
@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    onDestinationSelected: (BottomBarDestination) -> Unit = {},
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory()),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SecureUpTopBar(title = stringResource(id = R.string.profile_title))
        },
        bottomBar = {
            SecureUpBottomBar(
                currentDestination = BottomBarDestination.Profile,
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
                .padding(Spacing.L),
            verticalArrangement = Arrangement.spacedBy(Spacing.L),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.S),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(SecureUpColors.Primary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.email.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineLarge,
                        color = SecureUpColors.Primary,
                    )
                }
                Text(
                    text = uiState.email.ifBlank { stringResource(id = R.string.profile_no_session) },
                    style = MaterialTheme.typography.titleLarge,
                    color = SecureUpColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(id = R.string.profile_version, uiState.versionName),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecureUpColors.TextSecondary,
                )
            }

            ProfileInfoCard(
                title = stringResource(id = R.string.profile_privacy_title),
                body = stringResource(id = R.string.profile_privacy_body),
            )

            ProfileInfoCard(
                title = stringResource(id = R.string.profile_coming_soon_title),
                body = stringResource(id = R.string.profile_coming_soon_body),
            )

            SecureUpButton(
                text = stringResource(id = R.string.profile_logout_button),
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                variant = SecureUpButtonVariant.Danger,
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(text = stringResource(id = R.string.profile_logout_dialog_title))
            },
            text = {
                Text(text = stringResource(id = R.string.profile_logout_dialog_text))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLoggedOut()
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.profile_logout_confirm),
                        color = SecureUpColors.Danger,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = stringResource(id = R.string.dialog_cancel),
                        color = SecureUpColors.TextSecondary,
                    )
                }
            },
            containerColor = SecureUpColors.Surface,
            titleContentColor = SecureUpColors.TextPrimary,
            textContentColor = SecureUpColors.TextSecondary,
        )
    }
}

@Composable
private fun ProfileInfoCard(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.Card))
            .background(SecureUpColors.SurfaceVariant)
            .padding(Spacing.L),
        verticalArrangement = Arrangement.spacedBy(Spacing.S),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = SecureUpColors.TextPrimary,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = SecureUpColors.TextSecondary,
        )
    }
}

@Preview(name = "ProfileScreen", showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    SecureUPTheme {
        ProfileScreen(onLoggedOut = {})
    }
}
