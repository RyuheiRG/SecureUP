package com.secureup.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.secureup.app.R
import com.secureup.app.ui.components.ButtonStyle
import com.secureup.app.ui.components.SecureUpButton
import com.secureup.app.ui.components.SecureUpTextField
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing


@Composable
fun LoginScreen(onRegisterClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.Xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.Xxl, Alignment.CenterVertically),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
                Text(
                    text = stringResource(id = R.string.login_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = SecureUpColors.TextPrimary,
                )
                Text(
                    text = stringResource(id = R.string.login_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecureUpColors.TextSecondary,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Xl)) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.Xl)) {
                    SecureUpTextField(
                        label = stringResource(id = R.string.login_email_label),
                        value = email,
                        onValueChange = { email = it },
                        placeholder = stringResource(id = R.string.login_email_placeholder),
                    )
                    SecureUpTextField(
                        label = stringResource(id = R.string.login_password_label),
                        value = password,
                        onValueChange = { password = it },
                        isPassword = true,
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.L),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SecureUpButton(
                        text = stringResource(id = R.string.login_submit),
                        onClick = { /* TODO(paso 6): conectar AuthViewModel */ },
                        modifier = Modifier.fillMaxWidth(),
                        style = ButtonStyle.Primary,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(id = R.string.login_no_account),
                            style = MaterialTheme.typography.bodySmall,
                            color = SecureUpColors.TextSecondary,
                        )
                        Text(
                            text = stringResource(id = R.string.login_create_account),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onRegisterClick() },
                        )
                    }
                }

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