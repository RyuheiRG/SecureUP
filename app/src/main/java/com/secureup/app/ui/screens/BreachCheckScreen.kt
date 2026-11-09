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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.secureup.app.R
import com.secureup.app.ui.components.BottomBarDestination
import com.secureup.app.ui.components.InfoBanner
import com.secureup.app.ui.components.InfoBannerVariant
import com.secureup.app.ui.components.SecureUpBottomBar
import com.secureup.app.ui.components.SecureUpButton
import com.secureup.app.ui.components.SecureUpButtonVariant
import com.secureup.app.ui.components.SecureUpTextField
import com.secureup.app.ui.components.SecureUpTopBar
import com.secureup.app.ui.components.StatusBadge
import com.secureup.app.ui.components.StatusBadgeVariant
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing
import com.secureup.app.viewmodel.BreachCheckOutcome
import com.secureup.app.viewmodel.BreachViewModel
import java.util.Locale

/**
 * Verificador de brechas (Frame A06 - Verificador de brechas).
 *
 * La contraseña se escribe solo para consultarla; el ViewModel la borra del
 * estado en cuanto termina la petición.
 */
@Composable
fun BreachCheckScreen(
    onDestinationSelected: (BottomBarDestination) -> Unit = {},
    viewModel: BreachViewModel = viewModel(factory = BreachViewModel.Factory()),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            SecureUpTopBar(title = stringResource(id = R.string.breach_title))
        },
        bottomBar = {
            SecureUpBottomBar(
                currentDestination = BottomBarDestination.Breach,
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
            Text(
                text = stringResource(id = R.string.breach_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = SecureUpColors.TextSecondary,
            )

            InfoBanner(
                text = stringResource(id = R.string.breach_privacy_note),
                variant = InfoBannerVariant.Warning,
            )

            SecureUpTextField(
                label = stringResource(id = R.string.breach_password_label),
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                isPassword = true,
                placeholder = stringResource(id = R.string.breach_password_placeholder),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.checkPassword() }),
            )

            SecureUpButton(
                text = stringResource(id = R.string.breach_check_button),
                onClick = viewModel::checkPassword,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.password.isNotBlank() && !uiState.isLoading,
            )

            if (uiState.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        color = SecureUpColors.Primary,
                        modifier = Modifier.size(Spacing.Xl),
                    )
                    Spacer(modifier = Modifier.size(Spacing.M))
                    Text(
                        text = stringResource(id = R.string.breach_checking),
                        style = MaterialTheme.typography.bodySmall,
                        color = SecureUpColors.TextSecondary,
                    )
                }
            }

            uiState.errorRes?.let { errorRes ->
                InfoBanner(
                    text = stringResource(id = errorRes),
                    variant = InfoBannerVariant.Danger,
                )
            }

            uiState.outcome?.let { outcome ->
                when (outcome) {
                    BreachCheckOutcome.NotFound -> BreachOutcomeCard(
                        badgeVariant = StatusBadgeVariant.Safe,
                        badgeText = stringResource(id = R.string.breach_not_found_badge),
                        title = stringResource(id = R.string.breach_not_found_title),
                        description = stringResource(id = R.string.breach_not_found_description),
                    )

                    is BreachCheckOutcome.Found -> BreachOutcomeCard(
                        badgeVariant = StatusBadgeVariant.Dangerous,
                        badgeText = stringResource(id = R.string.breach_found_badge),
                        title = stringResource(id = R.string.breach_found_title),
                        description = stringResource(
                            id = R.string.breach_found_description,
                            formatOccurrences(outcome.occurrences),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun BreachOutcomeCard(
    badgeVariant: StatusBadgeVariant,
    badgeText: String,
    title: String,
    description: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.Card))
            .background(SecureUpColors.SurfaceVariant)
            .padding(Spacing.L),
        verticalArrangement = Arrangement.spacedBy(Spacing.M),
    ) {
        StatusBadge(text = badgeText, variant = badgeVariant)
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = SecureUpColors.TextPrimary,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = SecureUpColors.TextSecondary,
        )
    }
}

private fun formatOccurrences(occurrences: Long): String =
    String.format(Locale.getDefault(), "%,d", occurrences)

@Preview(name = "BreachCheckScreen", showBackground = true)
@Composable
private fun BreachCheckScreenPreview() {
    SecureUPTheme {
        BreachCheckScreen()
    }
}
