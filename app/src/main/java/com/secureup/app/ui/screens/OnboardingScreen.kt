package com.secureup.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.secureup.app.ui.components.SecureUpButton
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val pages = listOf(
    OnboardingPage(
        Icons.Filled.Key,
        "Tus contraseñas, cifradas en tu dispositivo",
        "Toda tu información se protege de forma local con cifrado AES-256. Nadie más puede acceder a ella."
    ),
    OnboardingPage(
        Icons.Filled.QrCodeScanner,
        "Verifica sin exponer tus datos",
        "Revisa si tus contraseñas fueron filtradas y escanea códigos QR sin compartir información sensible."
    )
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit = {}) {
    var current by remember { mutableIntStateOf(0) }
    val page = pages[current]
    val isLast = current == pages.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureUpColors.Background)
            .padding(Spacing.Xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(Radius.Card))
                .background(SecureUpColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                page.icon,
                contentDescription = null,
                tint = SecureUpColors.Primary,
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(Modifier.height(Spacing.Xl))
        Text(
            page.title,
            style = MaterialTheme.typography.titleLarge,
            color = SecureUpColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.S))
        Text(
            page.description,
            style = MaterialTheme.typography.bodySmall,
            color = SecureUpColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(Spacing.Xl))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            pages.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == current) 10.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (index == current) SecureUpColors.Primary else SecureUpColors.Outline)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        SecureUpButton(
            text = if (isLast) "Comenzar" else "Siguiente",
            onClick = { if (isLast) onFinished() else current++ }
        )
    }
}