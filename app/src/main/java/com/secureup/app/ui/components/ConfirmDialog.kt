package com.secureup.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.secureup.app.ui.theme.SecureUpColors

/**
 * Diálogo de confirmación modal reutilizable para acciones críticas (ej. cerrar sesión).
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDanger: Boolean = true,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = SecureUpColors.TextPrimary,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = SecureUpColors.TextSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = if (isDanger) SecureUpColors.Danger else SecureUpColors.Primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = cancelText,
                    color = SecureUpColors.TextSecondary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        containerColor = SecureUpColors.Surface,
    )
}
