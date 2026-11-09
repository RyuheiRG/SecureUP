package com.secureup.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.secureup.app.R
import com.secureup.app.domain.model.ScanRecord
import com.secureup.app.domain.model.ScanVerdict
import com.secureup.app.ui.components.SecureUpButton
import com.secureup.app.ui.components.SecureUpButtonVariant
import com.secureup.app.ui.components.SecureUpTopBar
import com.secureup.app.ui.components.StatusBadge
import com.secureup.app.ui.components.StatusBadgeVariant
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing
import com.secureup.app.viewmodel.PdfExportState
import com.secureup.app.viewmodel.ScanHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Historial de escaneos (Frame "historial-qr").
 *
 * Los datos viven en Room; aquí solo se pintan y se ofrece exportarlos a PDF
 * mediante el selector de documentos del sistema (SAF).
 */
@Composable
fun ScanHistoryScreen(
    onBack: () -> Unit,
    onItemClick: (String) -> Unit,
    viewModel: ScanHistoryViewModel = viewModel(factory = ScanHistoryViewModel.Factory()),
) {
    val uiState by viewModel.uiState.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    val exportFileName = stringResource(id = R.string.history_export_file_name)

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let(viewModel::exportPdf)
    }

    Scaffold(
        topBar = {
            SecureUpTopBar(
                title = stringResource(id = R.string.history_title),
                onBackClick = onBack,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.L),
            verticalArrangement = Arrangement.spacedBy(Spacing.M),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.L),
                horizontalArrangement = Arrangement.spacedBy(Spacing.M),
            ) {
                SecureUpButton(
                    text = stringResource(id = R.string.history_export_button),
                    onClick = {
                        exportLauncher.launch(exportFileName)
                    },
                    modifier = Modifier.weight(1f),
                    variant = SecureUpButtonVariant.Secondary,
                    enabled = uiState.items.isNotEmpty() && exportState != PdfExportState.Working,
                )
                SecureUpButton(
                    text = stringResource(id = R.string.history_clear_button),
                    onClick = { showClearDialog = true },
                    modifier = Modifier.weight(1f),
                    variant = SecureUpButtonVariant.Danger,
                    enabled = uiState.items.isNotEmpty(),
                )
            }

            when (val state = exportState) {
                PdfExportState.Done -> Text(
                    text = stringResource(id = R.string.history_export_success),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecureUpColors.Success,
                )

                is PdfExportState.Error -> Text(
                    text = stringResource(id = state.messageRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecureUpColors.Danger,
                )

                else -> Unit
            }

            if (uiState.items.isEmpty() && !uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.Xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(id = R.string.history_empty_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = SecureUpColors.TextPrimary,
                    )
                    Text(
                        text = stringResource(id = R.string.history_empty_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = SecureUpColors.TextSecondary,
                        modifier = Modifier.padding(top = Spacing.S),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.M),
                    contentPadding = PaddingValues(
                        top = Spacing.S,
                        bottom = Spacing.Xl,
                    ),
                ) {
                    items(items = uiState.items, key = { it.id }) { record ->
                        HistoryItem(record = record, onClick = { onItemClick(record.url) })
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(text = stringResource(id = R.string.history_clear_dialog_title))
            },
            text = {
                Text(text = stringResource(id = R.string.history_clear_dialog_text))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAll()
                        showClearDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.history_clear_confirm),
                        color = SecureUpColors.Danger,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
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
private fun HistoryItem(record: ScanRecord, onClick: () -> Unit) {
    val badgeVariant = when (record.verdict) {
        ScanVerdict.SAFE -> StatusBadgeVariant.Safe
        ScanVerdict.SUSPICIOUS -> StatusBadgeVariant.Suspicious
        ScanVerdict.DANGEROUS -> StatusBadgeVariant.Dangerous
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.Card))
            .background(SecureUpColors.SurfaceVariant)
            .clickable(onClick = onClick)
            .padding(Spacing.L),
        horizontalArrangement = Arrangement.spacedBy(Spacing.M),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.S),
        ) {
            Text(
                text = record.url,
                style = MaterialTheme.typography.bodyLarge,
                color = SecureUpColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatScanDate(record.scannedAt),
                style = MaterialTheme.typography.bodySmall,
                color = SecureUpColors.TextSecondary,
            )
        }
        StatusBadge(text = stringResource(id = record.verdict.badgeRes()), variant = badgeVariant)
    }
}

private fun formatScanDate(timestamp: Long): String =
    SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp))

private fun ScanVerdict.badgeRes(): Int = when (this) {
    ScanVerdict.SAFE -> R.string.result_badge_safe
    ScanVerdict.SUSPICIOUS -> R.string.result_badge_suspicious
    ScanVerdict.DANGEROUS -> R.string.result_badge_dangerous
}

@Preview(name = "ScanHistoryScreen", showBackground = true)
@Composable
private fun ScanHistoryScreenPreview() {
    SecureUPTheme {
        ScanHistoryScreen(onBack = {}, onItemClick = {})
    }
}
