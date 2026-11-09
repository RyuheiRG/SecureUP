package com.secureup.app.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.secureup.app.ui.components.BottomBarDestination
import com.secureup.app.ui.screens.BreachCheckScreen
import com.secureup.app.ui.screens.HomeScreen
import com.secureup.app.ui.screens.LoginScreen
import com.secureup.app.ui.screens.OnboardingScreen
import com.secureup.app.ui.screens.ProfileScreen
import com.secureup.app.ui.screens.RegisterScreen
import com.secureup.app.ui.screens.ScanHistoryScreen
import com.secureup.app.ui.screens.ScanResultScreen
import com.secureup.app.ui.screens.ScannerScreen
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
    const val BREACH = "breach"
    const val SCANNER = "scanner"
    const val SCAN_HISTORY = "scanner/history"
    const val PROFILE = "profile"

    const val SCAN_RESULT_ARG_URL = "url"
    const val SCAN_RESULT = "scanner/result/{$SCAN_RESULT_ARG_URL}"

    /** Codifica la URL para que las barras no rompan el patrón de ruta. */
    fun scanResult(url: String): String = "scanner/result/${Uri.encode(url)}"
}

/**
 * Grafo principal de navegación de SecureUP.
 *
 * - Splash avanza a Onboarding tras un breve retardo.
 * - Login ↔ Registro alternan con launchSingleTop.
 * - Al autenticarse se limpia la pila completa y se navega a Home.
 * - Las pestañas inferiores comparten Home como raíz y conservan su estado.
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
                    navController.navigateHome()
                },
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                // Registro siempre se abre desde Login, así que popBackStack reutiliza
                // la entrada existente. navigate + launchSingleTop no sirve: solo
                // deduplica cuando el destino ya es el tope de la pila.
                onLoginClick = {
                    navController.popBackStack()
                },
                onAuthenticated = {
                    navController.navigateHome()
                },
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onBreachClick = {
                    navController.navigateToTab(BottomBarDestination.Breach)
                },
                onScanQrClick = {
                    navController.navigateToTab(BottomBarDestination.Scanner)
                },
                onDestinationSelected = navController::navigateToTab,
            )
        }

        composable(Routes.BREACH) {
            BreachCheckScreen(
                onDestinationSelected = navController::navigateToTab,
            )
        }

        composable(Routes.SCANNER) {
            ScannerScreen(
                onResult = { url ->
                    navController.navigate(Routes.scanResult(url))
                },
                onHistoryClick = {
                    navController.navigate(Routes.SCAN_HISTORY)
                },
                onDestinationSelected = navController::navigateToTab,
            )
        }

        composable(
            route = Routes.SCAN_RESULT,
            arguments = listOf(navArgument(Routes.SCAN_RESULT_ARG_URL) { type = NavType.StringType }),
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString(Routes.SCAN_RESULT_ARG_URL).orEmpty()
            ScanResultScreen(
                url = url,
                onBack = { navController.popBackStack() },
                onScanAgain = {
                    if (!navController.popBackStack(Routes.SCANNER, inclusive = false)) {
                        navController.navigateToTab(BottomBarDestination.Scanner)
                    }
                },
                onHistoryClick = {
                    navController.navigate(Routes.SCAN_HISTORY)
                },
            )
        }

        composable(Routes.SCAN_HISTORY) {
            ScanHistoryScreen(
                onBack = { navController.popBackStack() },
                onItemClick = { url ->
                    navController.navigate(Routes.scanResult(url))
                },
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onDestinationSelected = navController::navigateToTab,
            )
        }
    }
}

/**
 * Cambia de pestaña conservando el estado de cada una. Home es la raíz: si se
 * navega desde una pestaña, "atrás" vuelve a Inicio.
 */
private fun NavHostController.navigateToTab(destination: BottomBarDestination) {
    val route = when (destination) {
        BottomBarDestination.Home -> Routes.HOME
        BottomBarDestination.Breach -> Routes.BREACH
        BottomBarDestination.Scanner -> Routes.SCANNER
        BottomBarDestination.Profile -> Routes.PROFILE
    }
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Navega al Dashboard dejando la pila vacía, para que "atrás" salga de la app
 * en vez de volver al formulario de acceso.
 */
private fun NavHostController.navigateHome() {
    navigate(Routes.HOME) {
        popUpTo(graph.id) { inclusive = true }
    }
}
