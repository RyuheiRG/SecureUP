package com.secureup.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.secureup.app.ui.theme.Radius
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit = {}) {
    LaunchedEffect(Unit) {
        delay(2000)
        onFinished()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(SecureUpColors.Background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(Radius.Card))
                .background(SecureUpColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = "Logo SecureUP",
                tint = SecureUpColors.Primary,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(Spacing.L))
        Text("SecureUP", style = MaterialTheme.typography.headlineLarge, color = SecureUpColors.TextPrimary)
        Spacer(Modifier.height(Spacing.Xs))
        Text(
            "Seguridad sin complicaciones",
            style = MaterialTheme.typography.bodySmall,
            color = SecureUpColors.TextSecondary
        )
    }
}