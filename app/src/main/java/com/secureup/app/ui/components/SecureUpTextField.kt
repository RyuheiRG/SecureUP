package com.secureup.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.secureup.app.ui.theme.FieldTextStyle
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

@Composable
fun SecureUpTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var visible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = SecureUpColors.TextSecondary)
        Spacer(Modifier.height(Spacing.Xs))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = FieldTextStyle,
            placeholder = { Text(placeholder, style = FieldTextStyle, color = SecureUpColors.TextSecondary) },
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(Radius.Control),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType
            ),
            visualTransformation =
                if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(
                            if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = SecureUpColors.TextSecondary
                        )
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SecureUpColors.Surface,
                unfocusedContainerColor = SecureUpColors.Surface,
                errorContainerColor = SecureUpColors.Surface,
                focusedBorderColor = SecureUpColors.Primary,
                unfocusedBorderColor = SecureUpColors.Outline,
                errorBorderColor = SecureUpColors.Danger,
                focusedTextColor = SecureUpColors.TextPrimary,
                unfocusedTextColor = SecureUpColors.TextPrimary,
                errorTextColor = SecureUpColors.TextPrimary,
                cursorColor = SecureUpColors.Primary
            )
        )

        if (error != null) {
            Spacer(Modifier.height(Spacing.Xs))
            Text(error, style = MaterialTheme.typography.bodySmall, color = SecureUpColors.Danger)
        }
    }
}