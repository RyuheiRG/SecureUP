package com.secureup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.secureup.app.R
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

enum class BottomBarDestination { Home, Vault, Scanner, Profile }

/**
 * Barra inferior de navegación de SecureUP.
 * 4 destinos: Inicio, Bóveda, Escáner, Perfil.
 * Destino activo en Primary con indicador de punto arriba del ícono.
 */
@Composable
fun SecureUpBottomBar(
    modifier: Modifier = Modifier,
    currentDestination: BottomBarDestination = BottomBarDestination.Home,
    onDestinationSelected: (BottomBarDestination) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SecureUpColors.Surface)
            .navigationBarsPadding()
            .padding(vertical = Spacing.S),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomBarItem(
            label = stringResource(id = R.string.nav_home),
            iconRes = R.drawable.ic_home,
            isSelected = currentDestination == BottomBarDestination.Home,
            onClick = { onDestinationSelected(BottomBarDestination.Home) },
        )
        BottomBarItem(
            label = stringResource(id = R.string.nav_vault),
            iconRes = R.drawable.ic_shield,
            isSelected = currentDestination == BottomBarDestination.Vault,
            onClick = { onDestinationSelected(BottomBarDestination.Vault) },
        )
        BottomBarItem(
            label = stringResource(id = R.string.nav_scanner),
            iconRes = R.drawable.ic_qr_code,
            isSelected = currentDestination == BottomBarDestination.Scanner,
            onClick = { onDestinationSelected(BottomBarDestination.Scanner) },
        )
        BottomBarItem(
            label = stringResource(id = R.string.nav_profile),
            iconRes = R.drawable.ic_user,
            isSelected = currentDestination == BottomBarDestination.Profile,
            onClick = { onDestinationSelected(BottomBarDestination.Profile) },
        )
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val activeColor = SecureUpColors.Primary
    val inactiveColor = SecureUpColors.TextSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            // `selectable` en vez de `clickable`: TalkBack anuncia "pestaña, seleccionada".
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.Tab,
            )
            .padding(horizontal = Spacing.M, vertical = Spacing.Xs),
    ) {
        // Punto indicador sobre el ícono activo
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(color = activeColor, shape = CircleShape),
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(Spacing.Xs))

        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.height(Spacing.Xs))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) activeColor else inactiveColor,
        )
    }
}

@Preview(name = "SecureUpBottomBar", showBackground = true)
@Composable
private fun SecureUpBottomBarPreview() {
    SecureUPTheme {
        SecureUpBottomBar()
    }
}
