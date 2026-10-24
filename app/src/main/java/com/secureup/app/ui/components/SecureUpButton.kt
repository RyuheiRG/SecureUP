package com.secureup.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUpColors

enum class ButtonStyle { Primary, Secondary, Danger }

@Composable
fun SecureUpButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(Radius.Control)
    val base = modifier.fillMaxWidth().heightIn(min = 52.dp)
    val label = @Composable { Text(text, style = MaterialTheme.typography.labelLarge) }

    when (style) {
        ButtonStyle.Primary -> Button(
            onClick = onClick, modifier = base, enabled = enabled, shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = SecureUpColors.Primary,
                contentColor = SecureUpColors.OnPrimary
            )
        ) { label() }

        ButtonStyle.Secondary -> OutlinedButton(
            onClick = onClick, modifier = base, enabled = enabled, shape = shape,
            border = BorderStroke(1.dp, SecureUpColors.Outline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SecureUpColors.TextPrimary)
        ) { label() }

        ButtonStyle.Danger -> Button(
            onClick = onClick, modifier = base, enabled = enabled, shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = SecureUpColors.Danger,
                contentColor = SecureUpColors.OnPrimary
            )
        ) { label() }
    }
}