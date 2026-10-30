package com.secureup.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.secureup.app.ui.screens.HomeScreen
import com.secureup.app.ui.screens.LoginScreen
import com.secureup.app.ui.screens.OnboardingScreen
import com.secureup.app.ui.screens.RegisterScreen
import com.secureup.app.ui.screens.SplashScreen

/**
 * Constantes de ruta para la navegación de la aplicación.
 */
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
}

/**
 * Grafo principal de navegación de SecureUP (Flujo de Acceso).
 *
 * Reglas de navegación:
 * - Splash avanza automáticamente a Onboarding tras un breve retardo.
 * - Onboarding ("Comenzar") remueve Splash y Onboarding de la pila y va a Login.
 * - Login ↔ Register alternan usando launchSingleTop.
 * - Al autenticarse (onAuthenticated), se limpia la pila completa y se navega a Home.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen {
                navController.navigate(Routes.ONBOARDING) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            }
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onRegisterClick = {
                    navController.navigate(Routes.REGISTER) {
                        launchSingleTop = true
                    }
                },
                onAuthenticated = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onLoginClick = {
                    navController.navigate(Routes.LOGIN) {
                        launchSingleTop = true
                    }
                },
                onAuthenticated = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            HomeScreen()
        }
    }
}
