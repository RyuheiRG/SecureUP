package com.secureup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.secureup.app.ui.screens.*
import com.secureup.app.ui.theme.SecureUPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SecureUPTheme {
                var screen by remember { mutableStateOf("splash") }

                when (screen) {
                    "splash" -> SplashScreen { screen = "onboarding" }
                    "onboarding" -> OnboardingScreen { screen = "login" }
                    "login" -> LoginScreen(onRegisterClick = { screen = "register" })
                    "register" -> RegisterScreen(onLoginClick = { screen = "login" })
                }
            }
        }
    }
}