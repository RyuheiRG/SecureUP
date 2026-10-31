package com.secureup.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.secureup.app.R
import com.secureup.app.ui.components.SecureUpButton
import com.secureup.app.ui.components.SecureUpButtonVariant
import com.secureup.app.ui.components.SecureUpTextField
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing
import com.secureup.app.viewmodel.AuthViewModel
import com.secureup.app.viewmodel.LoginUiState

/**
 * Login (frame auth02-login, nodo 87:204).
 * Lee el estado del AuthViewModel y emite eventos sin contener lógica de negocio.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory()),
    onRegisterClick: () -> Unit = {},
    onAuthenticated: () -> Unit = {},
) {
    val uiState by viewModel.loginUiState.collectAsStateWithLifecycle()

    LoginScreenContent(
        uiState = uiState,
        onEmailChange = viewModel::onLoginEmailChanged,
        onPasswordChange = viewModel::onLoginPasswordChanged,
        onLoginSubmit = { onAuthenticated() },
        onRegisterClick = onRegisterClick,
    )
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginSubmit: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val emailError = uiState.emailErrorRes?.let { stringResource(id = it) }
    val passwordError = (uiState.passwordErrorRes ?: uiState.generalErrorRes)?.let { stringResource(id = it) }

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
                    text = stringResource(id = R.string.login_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = SecureUpColors.TextPrimary,
                )
                Text(
                    text = stringResource(id = R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecureUpColors.TextSecondary,
                )
            }

            // Campos del formulario
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Xl)) {
                SecureUpTextField(
                    label = stringResource(id = R.string.login_email_label),
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    placeholder = stringResource(id = R.string.login_email_placeholder),
                    errorMessage = emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    ),
                )

                SecureUpTextField(
                    label = stringResource(id = R.string.login_password_label),
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    isPassword = true,
                    placeholder = stringResource(id = R.string.login_password_placeholder),
                    errorMessage = passwordError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onLoginSubmit()
                        },
                    ),
                )
            }

            Spacer(modifier = Modifier.height(Spacing.S))

            // Botón principal, enlace de registro y pie de página
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.L),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                SecureUpButton(
                    text = stringResource(id = R.string.login_submit),
                    onClick = onLoginSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    variant = SecureUpButtonVariant.Primary,
                    enabled = !uiState.isLoading,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.login_no_account),
                        style = MaterialTheme.typography.bodyLarge,
                        color = SecureUpColors.TextSecondary,
                    )
                    Text(
                        text = stringResource(id = R.string.login_create_account),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onRegisterClick() },
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.M))

                Text(
                    text = stringResource(id = R.string.login_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecureUpColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(name = "LoginScreen - Estado Normal", showBackground = true)
@Composable
private fun LoginScreenNormalPreview() {
    SecureUPTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginSubmit = {},
            onRegisterClick = {},
        )
    }
}

@Preview(name = "LoginScreen - Estado con Error", showBackground = true)
@Composable
private fun LoginScreenErrorPreview() {
    SecureUPTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                email = "usuario@correo.com",
                generalErrorRes = R.string.login_error,
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginSubmit = {},
            onRegisterClick = {},
        )
    }
}
