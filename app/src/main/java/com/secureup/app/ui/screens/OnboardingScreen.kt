package com.secureup.app.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.secureup.app.R
import com.secureup.app.ui.components.SecureUpButton
import com.secureup.app.ui.components.SecureUpButtonVariant
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

/**
 * Pantalla de Onboarding (Frames A02 - Onboarding 1 y A03 - Onboarding 2).
 * Gestiona la navegación fluida entre ambas páginas de introducción.
 *
 * @param initialPage Página inicial (0 = Onboarding 1, 1 = Onboarding 2).
 * @param onFinish Callback invocado al presionar "Comenzar" en la última página.
 */
@Composable
fun OnboardingScreen(
    initialPage: Int = 0,
    onFinish: () -> Unit = {},
) {
    var currentPage by remember { mutableIntStateOf(initialPage) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = Spacing.Xl,
                    vertical = Spacing.Xl,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Contenedor / Ilustración superior con transición entre páginas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = SecureUpColors.SurfaceVariant,
                        shape = RoundedCornerShape(24.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Crossfade(
                    targetState = currentPage,
                    label = "onboarding_illustration",
                ) { page ->
                    if (page == 0) {
                        // Ilustración Onboarding 1: Llave
                        Icon(
                            painter = painterResource(id = R.drawable.ic_key_round),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(120.dp),
                        )
                    } else {
                        // Ilustración Onboarding 2: Marco QR + Código QR
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_frame_qr),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(120.dp),
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.ic_qr_code),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(56.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.Xxl))

            // Textos explicativos según la página actual
            Crossfade(
                targetState = currentPage,
                label = "onboarding_texts",
            ) { page ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.M),
                ) {
                    Text(
                        text = stringResource(
                            id = if (page == 0) R.string.onboarding_1_title else R.string.onboarding_2_title,
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        color = SecureUpColors.TextPrimary,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text = stringResource(
                            id = if (page == 0) R.string.onboarding_1_description else R.string.onboarding_2_description,
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = SecureUpColors.TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.Xxl))

            // Indicador de páginas (Dots / Pager indicator)
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.S),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Punto / Píldora 1
                Box(
                    modifier = Modifier
                        .width(if (currentPage == 0) 24.dp else 6.dp)
                        .height(6.dp)
                        .background(
                            color = if (currentPage == 0) SecureUpColors.Primary else SecureUpColors.SurfaceVariant,
                            shape = if (currentPage == 0) RoundedCornerShape(3.dp) else CircleShape,
                        ),
                )

                // Punto / Píldora 2
                Box(
                    modifier = Modifier
                        .width(if (currentPage == 1) 24.dp else 6.dp)
                        .height(6.dp)
                        .background(
                            color = if (currentPage == 1) SecureUpColors.Primary else SecureUpColors.SurfaceVariant,
                            shape = if (currentPage == 1) RoundedCornerShape(3.dp) else CircleShape,
                        ),
                )
            }

            Spacer(modifier = Modifier.height(Spacing.Xxl))

            // Botón de acción principal
            SecureUpButton(
                text = stringResource(
                    id = if (currentPage == 0) R.string.onboarding_next else R.string.onboarding_start,
                ),
                onClick = {
                    if (currentPage == 0) {
                        currentPage = 1
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                variant = SecureUpButtonVariant.Primary,
            )
        }
    }
}
