package com.secureup.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.secureup.app.ui.components.PasswordStrengthBar
import com.secureup.app.ui.components.SecureUpBottomBar
import com.secureup.app.ui.components.SecureUpButton
import com.secureup.app.ui.components.SecureUpTextField
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

/**
 * Pantalla para cambiar la contraseña maestra del usuario.
 * Incluye medidor de fortaleza de contraseña (bajo, medio, alto / débil, regular, fuerte)
 * y tarjeta informativa sobre el re-cifrado de la bóveda.
 */
@Composable
fun ChangePasswordScreen(
    viewModel: ChangePasswordViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToTab: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onErrorMessageShown()
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
            // Barra superior con título y retroceso
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = SecureUpColors.TextPrimary,
                    )
                }
                Text(
                    text = stringResource(id = R.string.change_password_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = SecureUpColors.TextPrimary,
                )
            }

            // Campos de entrada de contraseñas
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.L)) {
                SecureUpTextField(
                    label = stringResource(id = R.string.change_password_current_label),
                    value = uiState.currentPassword,
                    onValueChange = { viewModel.onCurrentPasswordChange(it) },
                    isPassword = true,
                    placeholder = stringResource(id = R.string.change_password_placeholder),
                    error = uiState.currentPasswordError,
                )

                Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
                    SecureUpTextField(
                        label = stringResource(id = R.string.change_password_new_label),
                        value = uiState.newPassword,
                        onValueChange = { viewModel.onNewPasswordChange(it) },
                        isPassword = true,
                        placeholder = stringResource(id = R.string.change_password_placeholder),
                        error = uiState.newPasswordError,
                    )

                    // Medidor de fortaleza de contraseña
                    PasswordStrengthBar(
                        strength = uiState.newPasswordStrength,
                    )
                }

                SecureUpTextField(
                    label = stringResource(id = R.string.change_password_confirm_label),
                    value = uiState.confirmPassword,
                    onValueChange = { viewModel.onConfirmPasswordChange(it) },
                    isPassword = true,
                    placeholder = stringResource(id = R.string.change_password_placeholder),
                    error = uiState.confirmPasswordError,
                )
            }

            // Tarjeta de aviso de re-cifrado
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
                    horizontalArrangement = Arrangement.spacedBy(Spacing.M),
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = SecureUpColors.Warning,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = stringResource(id = R.string.change_password_warning_info),
                        style = MaterialTheme.typography.bodySmall,
                        color = SecureUpColors.TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón principal
            SecureUpButton(
                text = stringResource(id = R.string.change_password_submit_button),
                onClick = { viewModel.onChangePassword(onSuccess = onNavigateBack) },
                isLoading = uiState.isLoading,
                style = ButtonStyle.Primary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ChangePasswordScreenPreview() {
    val sampleViewModel = ChangePasswordViewModel(com.secureup.app.data.repository.FakeUserRepository())
    sampleViewModel.onNewPasswordChange("12345") // Establece un valor para mostrar la barra Débil en la vista previa

    SecureUPTheme {
        ChangePasswordScreen(
            viewModel = sampleViewModel,
            onNavigateBack = {},
        )
    }
}
