package com.secureup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.secureup.app.data.repository.FakeUserRepository
import com.secureup.app.ui.screens.LoginScreen
import com.secureup.app.ui.screens.OnboardingScreen
import com.secureup.app.ui.screens.RegisterScreen
import com.secureup.app.ui.screens.SplashScreen
import com.secureup.app.ui.screens.profile.ChangePasswordScreen
import com.secureup.app.ui.screens.profile.ChangePasswordViewModel
import com.secureup.app.ui.screens.profile.ProfileScreen
import com.secureup.app.ui.screens.profile.ProfileViewModel
import com.secureup.app.ui.theme.SecureUPTheme

/**
 * Actividad principal de SecureUP.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Repositorio e instancias de ViewModel para la demostración del módulo Perfil
        val userRepository = FakeUserRepository()
        val profileViewModel = ProfileViewModel(userRepository)
        val changePasswordViewModel = ChangePasswordViewModel(userRepository)

        setContent {
            SecureUPTheme {
                var screen by remember { mutableStateOf("profile") }

                when (screen) {
                    "splash" -> SplashScreen { screen = "onboarding" }
                    "onboarding" -> OnboardingScreen { screen = "login" }
                    "login" -> LoginScreen(onRegisterClick = { screen = "register" })
                    "register" -> RegisterScreen(onLoginClick = { screen = "login" })
                    "profile" -> ProfileScreen(
                        viewModel = profileViewModel,
                        onChangeMasterPasswordClick = { screen = "change_password" },
                        onLogoutSuccess = { screen = "login" },
                    )
                    "change_password" -> ChangePasswordScreen(
                        viewModel = changePasswordViewModel,
                        onNavigateBack = { screen = "profile" },
                    )
                }
            }
        }
    }
}
