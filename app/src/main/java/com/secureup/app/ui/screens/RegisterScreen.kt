package com.secureup.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.secureup.app.R
import com.secureup.app.ui.components.InfoBanner
import com.secureup.app.ui.components.InfoBannerVariant
import com.secureup.app.ui.components.SecureUpButton
import com.secureup.app.ui.components.SecureUpButtonVariant
import com.secureup.app.ui.components.SecureUpTextField
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing
import com.secureup.app.viewmodel.AuthViewModel
import com.secureup.app.viewmodel.RegisterUiState

/**
 * Registro (frame auth01-registro, nodo 87:153).
 * Lee el estado del AuthViewModel y emite eventos sin lógica de negocio.
 */
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory()),
    onLoginClick: () -> Unit = {},
    onAuthenticated: () -> Unit = {},
) {
    val uiState by viewModel.registerUiState.collectAsStateWithLifecycle()

    RegisterScreenContent(
        uiState = uiState,
        onEmailChange = viewModel::onRegisterEmailChanged,
        onMasterPasswordChange = viewModel::onRegisterMasterPasswordChanged,
        onConfirmMasterPasswordChange = viewModel::onRegisterConfirmPasswordChanged,
        onRegisterSubmit = { onAuthenticated() },
        onLoginClick = onLoginClick,
    )
}

@Composable
fun RegisterScreenContent(
    uiState: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onMasterPasswordChange: (String) -> Unit,
    onConfirmMasterPasswordChange: (String) -> Unit,
    onRegisterSubmit: () -> Unit,
    onLoginClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.Xl, vertical = Spacing.Xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.Xl),
        ) {
            // Encabezado
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
                Text(
                    text = stringResource(id = R.string.register_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = SecureUpColors.TextPrimary,
                )
                Text(
                    text = stringResource(id = R.string.register_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecureUpColors.TextSecondary,
                )
            }

            // Campos del formulario
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Xl)) {
                SecureUpTextField(
                    label = stringResource(id = R.string.register_email_label),
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    placeholder = stringResource(id = R.string.register_email_placeholder),
                    errorMessage = uiState.emailError,
                )

                SecureUpTextField(
                    label = stringResource(id = R.string.register_password_label),
                    value = uiState.masterPassword,
                    onValueChange = onMasterPasswordChange,
                    isPassword = true,
                    placeholder = stringResource(id = R.string.register_password_placeholder),
                    errorMessage = uiState.masterPasswordError,
                )

                SecureUpTextField(
                    label = stringResource(id = R.string.register_confirm_password_label),
                    value = uiState.confirmMasterPassword,
                    onValueChange = onConfirmMasterPasswordChange,
                    isPassword = true,
                    placeholder = stringResource(id = R.string.register_confirm_password_placeholder),
                    errorMessage = uiState.confirmMasterPasswordError ?: uiState.generalError,
                )

                InfoBanner(
                    text = stringResource(id = R.string.register_warning),
                    variant = InfoBannerVariant.Danger,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.S))

            // Botón principal y enlace
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.L),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                SecureUpButton(
                    text = stringResource(id = R.string.register_submit),
                    onClick = onRegisterSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    variant = SecureUpButtonVariant.Primary,
                    enabled = !uiState.isLoading,
                )

                Text(
                    text = stringResource(id = R.string.register_have_account),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecureUpColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLoginClick() },
                )
            }
        }
    }
}

@Preview(name = "RegisterScreen", showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    SecureUPTheme {
        RegisterScreenContent(
            uiState = RegisterUiState(),
            onEmailChange = {},
            onMasterPasswordChange = {},
            onConfirmMasterPasswordChange = {},
            onRegisterSubmit = {},
            onLoginClick = {},
        )
    }
}
