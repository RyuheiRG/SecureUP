package com.secureup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.secureup.app.R
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

/**
 * Niveles de fortaleza de contraseña.
 */
enum class PasswordStrength {
    None,
    Weak,
    Fair,
    Good,
    Strong,
}

/**
 * Calcula el nivel de fortaleza de una contraseña basándose en su longitud y diversidad de caracteres.
 */
fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.None
    var score = 0
    if (password.length >= 8) score++
    if (password.length >= 12) score++
    if (password.any { it.isDigit() } && password.any { it.isLetter() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when (score) {
        0, 1 -> PasswordStrength.Weak
        2 -> PasswordStrength.Fair
        3 -> PasswordStrength.Good
        4 -> PasswordStrength.Strong
        else -> PasswordStrength.Strong
    }
}

/**
 * Componente medidor de fortaleza de contraseña con 4 segmentos e indicador de texto.
 * Muestra segmentos en rojo (Débil), ámbar (Regular), azul (Buena) y verde (Fuerte).
 */
@Composable
fun PasswordStrengthBar(
    strength: PasswordStrength,
    modifier: Modifier = Modifier,
) {
    if (strength == PasswordStrength.None) return

    val (activeCount, color, labelRes) = when (strength) {
        PasswordStrength.None -> Triple(0, SecureUpColors.SurfaceVariant, R.string.strength_weak)
        PasswordStrength.Weak -> Triple(1, SecureUpColors.Danger, R.string.strength_weak)
        PasswordStrength.Fair -> Triple(2, SecureUpColors.Warning, R.string.strength_fair)
        PasswordStrength.Good -> Triple(3, SecureUpColors.Primary, R.string.strength_good)
        PasswordStrength.Strong -> Triple(4, SecureUpColors.Success, R.string.strength_strong)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.Xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.S),
        ) {
            for (i in 1..4) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = if (i <= activeCount) color else SecureUpColors.SurfaceVariant,
                            shape = RoundedCornerShape(2.dp),
                        ),
                )
            }
        }
        Text(
            text = stringResource(id = labelRes),
            style = MaterialTheme.typography.bodySmall,
            color = color,
        )
    }
}
