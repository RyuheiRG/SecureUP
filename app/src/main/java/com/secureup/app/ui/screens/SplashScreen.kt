package com.secureup.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.secureup.app.R
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing
import kotlinx.coroutines.delay

/**
 * Pantalla de Inicio / Splash (Frame A01 - Splash).
 * Muestra el título de la app y el eslogan centrados en la pantalla.
 * Avanza automáticamente después de un breve retardo (1.5 s).
 */
@Composable
fun SplashScreen(
    onTimeout: () -> Unit = {},
) {
    LaunchedEffect(Unit) {
        delay(1500L)
        onTimeout()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.Xl),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // TODO: Agregar el logo de la app (Image con R.drawable.ic_logo)

            Text(
                text = stringResource(id = R.string.splash_title),
                style = MaterialTheme.typography.headlineLarge,
                color = SecureUpColors.TextPrimary,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(id = R.string.splash_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = SecureUpColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.S),
            )
        }
    }
}
