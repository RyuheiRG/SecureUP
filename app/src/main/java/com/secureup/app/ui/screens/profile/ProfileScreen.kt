package com.secureup.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.secureup.app.R
import com.secureup.app.ui.components.ButtonStyle
import com.secureup.app.ui.components.ConfirmDialog
import com.secureup.app.ui.components.SecureUpBottomBar
import com.secureup.app.ui.components.SecureUpButton
import com.secureup.app.ui.components.SecureUpTextField
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

/**
 * Pantalla de Mi Perfil de SecureUP.
 * Muestra la información del usuario, formulario para editar nombre, acceso a cambio de contraseña
 * y botón para cerrar sesión.
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onChangeMasterPasswordClick: () -> Unit,
    onLogoutSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToTab: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onUserMessageShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SecureUpColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            SecureUpBottomBar(
                currentRoute = "profile",
                onNavigate = onNavigateToTab,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.L, vertical = Spacing.L),
            verticalArrangement = Arrangement.spacedBy(Spacing.Xl),
        ) {
            // Título principal
            Text(
                text = stringResource(id = R.string.profile_title),
                style = MaterialTheme.typography.titleLarge,
                color = SecureUpColors.TextPrimary,
            )

            // Tarjeta de información del usuario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.Card),
                colors = CardDefaults.cardColors(containerColor = SecureUpColors.Surface),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.L),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(SecureUpColors.Primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = uiState.user.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = SecureUpColors.OnPrimary,
                        )
                    }

                    Spacer(modifier = Modifier.width(Spacing.M))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.user.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = SecureUpColors.TextPrimary,
                            )
                            Spacer(modifier = Modifier.width(Spacing.Xs))
                            IconButton(
                                onClick = { viewModel.onToggleEditName() },
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(id = R.string.profile_edit_name_title),
                                    tint = SecureUpColors.TextSecondary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(Spacing.Xs))
                        Text(
                            text = uiState.user.email,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SecureUpColors.TextSecondary,
                        )
                    }
                }
            }

            // Sección opcional: Editar nombre
            if (uiState.isEditingName) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.Card),
                    colors = CardDefaults.cardColors(containerColor = SecureUpColors.Surface),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.L),
                        verticalArrangement = Arrangement.spacedBy(Spacing.M),
                    ) {
                        Text(
                            text = stringResource(id = R.string.profile_edit_name_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = SecureUpColors.TextPrimary,
                        )

                        SecureUpTextField(
                            label = stringResource(id = R.string.profile_name_label),
                            value = uiState.nameInput,
                            onValueChange = { viewModel.onNameInputChange(it) },
                            error = uiState.nameError,
                        )

                        SecureUpButton(
                            text = stringResource(id = R.string.profile_save_button),
                            onClick = { viewModel.onSaveName() },
                            isLoading = uiState.isSavingName,
                            style = ButtonStyle.Primary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // Fila: Último inicio de sesión
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = SecureUpColors.TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(Spacing.S))
                Text(
                    text = stringResource(
                        id = R.string.profile_last_login,
                        uiState.user.lastLogin,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecureUpColors.TextSecondary,
                )
            }

            // Sección SEGURIDAD
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
                Text(
                    text = stringResource(id = R.string.profile_security_section),
                    style = MaterialTheme.typography.labelMedium,
                    color = SecureUpColors.TextSecondary,
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChangeMasterPasswordClick() },
                    shape = RoundedCornerShape(Radius.Card),
                    colors = CardDefaults.cardColors(containerColor = SecureUpColors.Surface),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.L),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(id = R.string.profile_change_master_password),
                            style = MaterialTheme.typography.bodyLarge,
                            color = SecureUpColors.TextPrimary,
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SecureUpColors.TextSecondary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón destructivo Cerrar sesión
            SecureUpButton(
                text = stringResource(id = R.string.profile_logout_button),
                onClick = { viewModel.onShowLogoutDialog() },
                style = ButtonStyle.Danger,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    // Diálogo de confirmación para cerrar sesión
    if (uiState.showLogoutDialog) {
        ConfirmDialog(
            title = stringResource(id = R.string.profile_logout_dialog_title),
            message = stringResource(id = R.string.profile_logout_dialog_message),
            confirmText = stringResource(id = R.string.profile_logout_dialog_confirm),
            cancelText = stringResource(id = R.string.profile_logout_dialog_cancel),
            onConfirm = { viewModel.onConfirmLogout(onLogoutSuccess) },
            onDismiss = { viewModel.onDismissLogoutDialog() },
            isDanger = true,
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    com.secureup.app.ui.theme.SecureUPTheme {
        ProfileScreen(
            viewModel = ProfileViewModel(com.secureup.app.data.repository.FakeUserRepository()),
            onChangeMasterPasswordClick = {},
            onLogoutSuccess = {},
        )
    }
}

