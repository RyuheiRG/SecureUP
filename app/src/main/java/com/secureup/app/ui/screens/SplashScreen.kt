package com.secureup.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.secureup.app.R
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing
import kotlinx.coroutines.delay

/**
 * Pantalla de Inicio / Splash (Frame A01 - Splash).
 * Muestra el logo de la app y el eslogan centrados en la pantalla.
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
            Image(
                painter = painterResource(id = R.drawable.secureup_logo),
                contentDescription = null, // decorativo: el logo ya contiene el nombre
                modifier = Modifier.size(180.dp),
            )

            Spacer(modifier = Modifier.height(Spacing.L))

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
