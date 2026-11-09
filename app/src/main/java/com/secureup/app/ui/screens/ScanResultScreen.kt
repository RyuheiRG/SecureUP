package com.secureup.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.secureup.app.R
import com.secureup.app.domain.model.ScanVerdict
import com.secureup.app.domain.security.UrlRiskReason
import com.secureup.app.ui.components.InfoBanner
import com.secureup.app.ui.components.InfoBannerVariant
import com.secureup.app.ui.components.SecureUpButton
import com.secureup.app.ui.components.SecureUpButtonVariant
import com.secureup.app.ui.components.SecureUpTopBar
import com.secureup.app.ui.components.StatusBadge
import com.secureup.app.ui.components.StatusBadgeVariant
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing
import com.secureup.app.viewmodel.ScanResultViewModel

/**
 * Resultado del escaneo (Frames A09a/A09b/A09c: una sola pantalla por estado).
 *
 * Solo se ofrece abrir la URL cuando el veredicto es SAFE. Para sospechosas o
 * peligrosas se muestra el motivo y se invita a volver a escanear.
 */
@Composable
fun ScanResultScreen(
    url: String,
    onBack: () -> Unit,
    onScanAgain: () -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: ScanResultViewModel = viewModel(factory = ScanResultViewModel.factory(url)),
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    Scaffold(
        topBar = {
            SecureUpTopBar(
                title = stringResource(id = R.string.result_title),
                onBackClick = onBack,
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
            val badgeVariant = when (state.verdict) {
                ScanVerdict.SAFE -> StatusBadgeVariant.Safe
                ScanVerdict.SUSPICIOUS -> StatusBadgeVariant.Suspicious
                ScanVerdict.DANGEROUS -> StatusBadgeVariant.Dangerous
            }
            StatusBadge(
                text = stringResource(id = state.verdict.badgeRes()),
                variant = badgeVariant,
            )

            Text(
                text = stringResource(id = state.verdict.titleRes()),
                style = MaterialTheme.typography.headlineLarge,
                color = SecureUpColors.TextPrimary,
            )

            Text(
                text = stringResource(id = state.verdict.descriptionRes()),
                style = MaterialTheme.typography.bodyLarge,
                color = SecureUpColors.TextSecondary,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.Card))
                    .background(SecureUpColors.SurfaceVariant)
                    .padding(Spacing.L),
                verticalArrangement = Arrangement.spacedBy(Spacing.S),
            ) {
                Text(
                    text = stringResource(id = R.string.result_url_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = SecureUpColors.TextSecondary,
                )
                SelectionContainer {
                    Text(
                        text = state.url,
                        style = MaterialTheme.typography.bodyLarge,
                        color = SecureUpColors.TextPrimary,
                    )
                }
            }

            if (state.reasons.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.Card))
                        .background(SecureUpColors.Surface)
                        .padding(Spacing.L),
                    verticalArrangement = Arrangement.spacedBy(Spacing.M),
                ) {
                    Text(
                        text = stringResource(id = R.string.result_reasons_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = SecureUpColors.TextPrimary,
                    )
                    state.reasons.forEach { reason ->
                        RiskReasonRow(reason)
                    }
                }
            }

            if (state.verdict != ScanVerdict.SAFE) {
                InfoBanner(
                    text = stringResource(id = R.string.result_no_open_note),
                    variant = InfoBannerVariant.Danger,
                )
            }

            if (state.verdict == ScanVerdict.SAFE) {
                SecureUpButton(
                    text = stringResource(id = R.string.result_open_button),
                    onClick = {
                        openUrl(context = context, url = state.url)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            SecureUpButton(
                text = stringResource(id = R.string.result_scan_again_button),
                onClick = onScanAgain,
                modifier = Modifier.fillMaxWidth(),
                variant = SecureUpButtonVariant.Secondary,
            )

            SecureUpButton(
                text = stringResource(id = R.string.result_history_button),
                onClick = onHistoryClick,
                modifier = Modifier.fillMaxWidth(),
                variant = SecureUpButtonVariant.Secondary,
            )
        }
    }
}

@Composable
private fun RiskReasonRow(reason: UrlRiskReason) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.M),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_alert_triangle),
            contentDescription = null,
            tint = SecureUpColors.Warning,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(id = reason.messageRes()),
            style = MaterialTheme.typography.bodySmall,
            color = SecureUpColors.TextSecondary,
        )
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    val uri = url.toUri()
    if (uri.scheme != "http" && uri.scheme != "https") return
    val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, R.string.result_no_browser, Toast.LENGTH_SHORT).show()
    }
}

private fun ScanVerdict.badgeRes(): Int = when (this) {
    ScanVerdict.SAFE -> R.string.result_badge_safe
    ScanVerdict.SUSPICIOUS -> R.string.result_badge_suspicious
    ScanVerdict.DANGEROUS -> R.string.result_badge_dangerous
}

private fun ScanVerdict.titleRes(): Int = when (this) {
    ScanVerdict.SAFE -> R.string.result_title_safe
    ScanVerdict.SUSPICIOUS -> R.string.result_title_suspicious
    ScanVerdict.DANGEROUS -> R.string.result_title_dangerous
}

private fun ScanVerdict.descriptionRes(): Int = when (this) {
    ScanVerdict.SAFE -> R.string.result_description_safe
    ScanVerdict.SUSPICIOUS -> R.string.result_description_suspicious
    ScanVerdict.DANGEROUS -> R.string.result_description_dangerous
}

private fun UrlRiskReason.messageRes(): Int = when (this) {
    UrlRiskReason.INVALID_URL -> R.string.result_reason_invalid
    UrlRiskReason.UNSUPPORTED_SCHEME -> R.string.result_reason_scheme
    UrlRiskReason.NO_TLS -> R.string.result_reason_no_tls
    UrlRiskReason.IP_HOST -> R.string.result_reason_ip
    UrlRiskReason.PUNYCODE -> R.string.result_reason_punycode
    UrlRiskReason.USER_INFO -> R.string.result_reason_user_info
    UrlRiskReason.SHORTENER -> R.string.result_reason_shortener
    UrlRiskReason.LONG_URL -> R.string.result_reason_long
}

@Preview(name = "ScanResultScreen - seguro", showBackground = true)
@Composable
private fun ScanResultScreenPreview() {
    SecureUPTheme {
        ScanResultScreen(
            url = "https://example.com",
            onBack = {},
            onScanAgain = {},
            onHistoryClick = {},
        )
    }
}
