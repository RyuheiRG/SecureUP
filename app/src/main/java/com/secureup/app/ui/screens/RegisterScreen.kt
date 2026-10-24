package com.secureup.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.secureup.app.ui.components.SecureUpButton
import com.secureup.app.ui.components.SecureUpTextField
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

@Composable
fun RegisterScreen(
    onRegisterClick: (String, String) -> Unit = { _, _ -> },
    onLoginClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureUpColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(Spacing.Xl),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineLarge, color = SecureUpColors.TextPrimary)
        Spacer(Modifier.height(Spacing.Xs))
        Text(
            "Registra tu cuenta para proteger tus credenciales",
            style = MaterialTheme.typography.bodySmall,
            color = SecureUpColors.TextSecondary
        )
        Spacer(Modifier.height(Spacing.Xl))

        SecureUpTextField(
            label = "Correo electrónico",
            value = email,
            onValueChange = { email = it },
            placeholder = "ejemplo@correo.com",
            keyboardType = KeyboardType.Email,
            error = emailError
        )
        Spacer(Modifier.height(Spacing.L))

        SecureUpTextField(
            label = "Contraseña maestra",
            value = password,
            onValueChange = { password = it },
            placeholder = "Escribe tu contraseña maestra",
            isPassword = true,
            error = passwordError
        )
        Spacer(Modifier.height(Spacing.L))

        SecureUpTextField(
            label = "Confirmar contraseña",
            value = confirm,
            onValueChange = { confirm = it },
            placeholder = "Repite tu contraseña maestra",
            isPassword = true,
            error = confirmError
        )
        Spacer(Modifier.height(Spacing.L))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.Control))
                .background(SecureUpColors.Danger.copy(alpha = 0.12f))
                .padding(Spacing.M),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = SecureUpColors.Danger,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(Spacing.S))
            Text(
                "Si olvidas tu contraseña maestra no podremos recuperarla. Guárdala en un lugar seguro.",
                style = MaterialTheme.typography.bodySmall,
                color = SecureUpColors.Danger
            )
        }

        Spacer(Modifier.height(Spacing.Xl))

        SecureUpButton(
            text = "Crear cuenta",
            onClick = {
                emailError = if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
                    "Ingresa un correo válido" else null
                passwordError = if (password.length < 8) "Mínimo 8 caracteres" else null
                confirmError = if (confirm != password) "Las contraseñas no coinciden" else null

                if (emailError == null && passwordError == null && confirmError == null) {
                    onRegisterClick(email, password)
                }
            }
        )

        Spacer(Modifier.height(Spacing.L))
        Text(
            "¿Ya tienes cuenta? Inicia sesión",
            style = MaterialTheme.typography.bodySmall,
            color = SecureUpColors.Primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clickable { onLoginClick() }
        )
    }
}