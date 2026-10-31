package com.secureup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.secureup.app.R
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing

enum class NavItem(val route: String, val labelRes: Int, val icon: ImageVector) {
    Home("home", R.string.nav_home, Icons.Default.Home),
    Vault("vault", R.string.nav_vault, Icons.Default.Shield),
    Scanner("scanner", R.string.nav_scanner, Icons.Default.QrCodeScanner),
    Profile("profile", R.string.nav_profile, Icons.Default.Person),
}

/**
 * Barra de navegación inferior de SecureUP.
 * Muestra las 4 pestañas del diseño con el indicador activo en forma de punto.
 */
@Composable
fun SecureUpBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SecureUpColors.Surface)
            .padding(vertical = Spacing.S),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavItem.entries.forEach { item ->
            val isSelected = currentRoute == item.route
            val color = if (isSelected) SecureUpColors.Primary else SecureUpColors.TextSecondary

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onNavigate(item.route) }
                    .padding(horizontal = Spacing.S, vertical = Spacing.Xs),
            ) {
                Box(contentAlignment = Alignment.TopCenter) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(SecureUpColors.Primary, CircleShape)
                        )
                    }
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(id = item.labelRes),
                        tint = color,
                        modifier = Modifier
                            .padding(top = if (isSelected) 6.dp else 0.dp)
                            .size(24.dp),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(id = item.labelRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                )
            }
        }
    }
}
