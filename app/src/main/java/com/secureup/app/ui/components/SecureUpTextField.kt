package com.secureup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.secureup.app.R
import com.secureup.app.ui.theme.FieldTextStyle
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

/** Métricas internas del campo. Fuente: Figma "TextField" (nodo 87:216). No están en la escala Spacing. */
private val FieldVerticalPadding = 14.dp
private val FieldBorderWidth = 1.dp
private val LabelBottomGap = 6.dp
private val EyeIconSize = 20.dp
private val EyeTouchTargetSize = 48.dp
private val ErrorIconSize = 16.dp

/**
 * Campo de texto de SecureUP, con etiqueta arriba.
 *
 * Estados (Figma):
 *  - Normal: fondo SurfaceVariant, borde SurfaceVariant (no visible), borde/etiqueta en TextSecondary.
 *  - Enfocado: borde Primary.
 *  - Error: borde Danger + mensaje con ícono de alerta debajo.
 *  - Contraseña: transformación visual + ícono de ojo para mostrar/ocultar.
 *
 * @param placeholder texto de ayuda que se muestra cuando el campo está vacío (extra, no estaba
 *        en la firma original, pero el diseño de login lo usa).
 * @param interactionSource permite simular el estado "focused" en los @Preview.
 */
@Composable
fun SecureUpTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    errorMessage: String? = null,
    placeholder: String? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val focused by interactionSource.collectIsFocusedAsState()
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val errorText = errorMessage

    val borderColor = when {
        errorText != null -> SecureUpColors.Danger
        focused -> SecureUpColors.Primary
        else -> SecureUpColors.SurfaceVariant
    }
    val shape = RoundedCornerShape(Radius.Control)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = SecureUpColors.TextSecondary,
        )
        Spacer(modifier = Modifier.height(LabelBottomGap))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(SecureUpColors.SurfaceVariant, shape)
                .border(FieldBorderWidth, borderColor, shape),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = Spacing.L,
                        top = FieldVerticalPadding,
                        bottom = FieldVerticalPadding,
                        end = if (isPassword) 0.dp else Spacing.L,
                    )
            ) {
                if (value.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = FieldTextStyle,
                        color = SecureUpColors.TextSecondary,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = FieldTextStyle.copy(color = SecureUpColors.TextPrimary),
                    cursorBrush = SolidColor(SecureUpColors.Primary),
                    interactionSource = interactionSource,
                    visualTransformation = if (isPassword && !passwordVisible) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                )
            }

            if (isPassword) {
                // IconButton garantiza un área táctil de 48 dp; el ícono visual se queda en 20 dp.
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.size(EyeTouchTargetSize),
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.ic_eye_off else R.drawable.ic_eye
                        ),
                        contentDescription = stringResource(
                            id = if (passwordVisible) R.string.password_hide else R.string.password_show
                        ),
                        tint = SecureUpColors.TextSecondary,
                        modifier = Modifier.size(EyeIconSize),
                    )
                }
            }
        }

        if (errorText != null) {
            Spacer(modifier = Modifier.height(Spacing.Xs))
            Row(
                modifier = Modifier.padding(horizontal = Spacing.Xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Xs),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_alert_triangle),
                    contentDescription = null,
                    tint = SecureUpColors.Danger,
                    modifier = Modifier.size(ErrorIconSize),
                )
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.bodySmall,
                    color = SecureUpColors.Danger,
                )
            }
        }
    }
}

@Preview(name = "SecureUpTextField - todos los estados")
@Composable
private fun SecureUpTextFieldPreview() {
    SecureUPTheme {
        Column(
            modifier = Modifier
                .background(SecureUpColors.Background)
                .padding(Spacing.L),
            verticalArrangement = Arrangement.spacedBy(Spacing.Xl),
        ) {
            SecureUpTextField(
                label = "Correo electrónico",
                value = "",
                onValueChange = {},
                placeholder = "usuario@correo.com",
            )
            SecureUpTextField(
                label = "Correo electrónico",
                value = "ana@secureup.app",
                onValueChange = {},
            )
            SecureUpFocusedFieldPreview()
            SecureUpTextField(
                label = "Contraseña maestra",
                value = "secreta123",
                onValueChange = {},
                isPassword = true,
            )
            SecureUpTextField(
                label = "Contraseña maestra",
                value = "",
                onValueChange = {},
                isPassword = true,
                errorMessage = "Credenciales incorrectas. Intenta de nuevo.",
            )
        }
    }
}

/** Muestra el estado "focused" emitiendo una interacción de foco. */
@Composable
private fun SecureUpFocusedFieldPreview() {
    val source = remember { MutableInteractionSource() }
    LaunchedEffect(source) { source.emit(FocusInteraction.Focus()) }
    SecureUpTextField(
        label = "Campo enfocado",
        value = "",
        onValueChange = {},
        placeholder = "Foco",
        interactionSource = source,
    )
}
