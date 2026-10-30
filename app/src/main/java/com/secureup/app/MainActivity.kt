package com.secureup.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.secureup.app.ui.navigation.AppNavigation
import com.secureup.app.ui.theme.SecureUPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // La app es solo tema oscuro: forzamos iconos claros en las barras del sistema.
        // Con el valor por defecto (SystemBarStyle.auto) los iconos serían oscuros si el
        // teléfono está en tema claro y desaparecerían sobre el fondo #0F1115.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            SecureUPTheme {
                AppNavigation()
            }
        }
    }
}
