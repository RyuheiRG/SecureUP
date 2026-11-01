package com.secureup.app.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.secureup.app.R
import com.secureup.app.data.repository.AuthRepository
import com.secureup.app.data.repository.AuthResult
import com.secureup.app.data.repository.FakeAuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la UI para la pantalla de Login.
 *
 * Los errores son IDs de `strings.xml`, no texto: la pantalla los resuelve con
 * `stringResource` y así el ViewModel no depende de recursos ni de idioma.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    @get:StringRes val emailErrorRes: Int? = null,
    @get:StringRes val passwordErrorRes: Int? = null,
    @get:StringRes val generalErrorRes: Int? = null,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
)

/**
 * Estado de la UI para la pantalla de Registro.
 */
data class RegisterUiState(
    val email: String = "",
    val masterPassword: String = "",
    val confirmMasterPassword: String = "",
    @get:StringRes val emailErrorRes: Int? = null,
    @get:StringRes val masterPasswordErrorRes: Int? = null,
    @get:StringRes val confirmMasterPasswordErrorRes: Int? = null,
    @get:StringRes val generalErrorRes: Int? = null,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
)

/**
 * ViewModel de Autenticación para Login y Registro.
 *
 * Reglas de seguridad:
 * - Mensaje de error de login SIEMPRE genérico ("Credenciales incorrectas. Intenta de nuevo.").
 * - Limpia las contraseñas del estado al finalizar cualquier intento (éxito o fallo).
 * - Prohibido loguear contraseñas, correos o resultados de autenticación.
 *
 * La navegación NO se hace aquí: el ViewModel solo publica `isAuthenticated` y la
 * pantalla reacciona en el hilo principal. Así no se toca el NavController desde
 * `ioDispatcher`.
 *
 * @param ioDispatcher se inyecta para que las pruebas usen un dispatcher de test
 *   (StandardTestDispatcher) y el flujo completo sea determinista.
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _registerUiState = MutableStateFlow(RegisterUiState())
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

    fun onLoginEmailChanged(email: String) {
        _loginUiState.update {
            it.copy(
                email = email,
                emailErrorRes = null,
                generalErrorRes = null,
            )
        }
    }

    fun onLoginPasswordChanged(password: String) {
        _loginUiState.update {
            it.copy(
                password = password,
                passwordErrorRes = null,
                generalErrorRes = null,
            )
        }
    }

    fun login() {
        val currentState = _loginUiState.value
        val validation = AuthValidator.validateLogin(currentState.email, currentState.password)

        if (!validation.isValid) {
            _loginUiState.update {
                it.copy(
                    password = "", // Limpieza de contraseña por seguridad
                    emailErrorRes = validation.emailErrorRes,
                    passwordErrorRes = validation.passwordErrorRes,
                )
            }
            return
        }

        _loginUiState.update {
            it.copy(
                isLoading = true,
                emailErrorRes = null,
                passwordErrorRes = null,
                generalErrorRes = null,
            )
        }

        viewModelScope.launch(ioDispatcher) {
            val result = authRepository.login(validation.sanitizedEmail, currentState.password)
            when (result) {
                is AuthResult.Success -> {
                    _loginUiState.update {
                        it.copy(
                            password = "", // Limpieza de contraseña
                            isLoading = false,
                            isAuthenticated = true,
                        )
                    }
                }
                is AuthResult.Failure -> {
                    _loginUiState.update {
                        it.copy(
                            password = "", // Limpieza de contraseña por seguridad
                            isLoading = false,
                            generalErrorRes = R.string.login_error,
                        )
                    }
                }
            }
        }
    }

    fun onRegisterEmailChanged(email: String) {
        _registerUiState.update {
            it.copy(
                email = email,
                emailErrorRes = null,
                generalErrorRes = null,
            )
        }
    }

    fun onRegisterMasterPasswordChanged(password: String) {
        _registerUiState.update {
            it.copy(
                masterPassword = password,
                masterPasswordErrorRes = null,
                generalErrorRes = null,
            )
        }
    }

    fun onRegisterConfirmPasswordChanged(password: String) {
        _registerUiState.update {
            it.copy(
                confirmMasterPassword = password,
                confirmMasterPasswordErrorRes = null,
                generalErrorRes = null,
            )
        }
    }

    fun register() {
        val currentState = _registerUiState.value
        val validation = AuthValidator.validateRegister(
            currentState.email,
            currentState.masterPassword,
            currentState.confirmMasterPassword,
        )

        if (!validation.isValid) {
            _registerUiState.update {
                it.copy(
                    masterPassword = "", // Limpieza de contraseña
                    confirmMasterPassword = "", // Limpieza de contraseña
                    emailErrorRes = validation.emailErrorRes,
                    masterPasswordErrorRes = validation.passwordErrorRes,
                    confirmMasterPasswordErrorRes = validation.confirmPasswordErrorRes,
                )
            }
            return
        }

        _registerUiState.update {
            it.copy(
                isLoading = true,
                emailErrorRes = null,
                masterPasswordErrorRes = null,
                confirmMasterPasswordErrorRes = null,
                generalErrorRes = null,
            )
        }

        viewModelScope.launch(ioDispatcher) {
            val result = authRepository.register(validation.sanitizedEmail, currentState.masterPassword)
            when (result) {
                is AuthResult.Success -> {
                    _registerUiState.update {
                        it.copy(
                            masterPassword = "", // Limpieza de contraseña
                            confirmMasterPassword = "", // Limpieza de contraseña
                            isLoading = false,
                            isAuthenticated = true,
                        )
                    }
                }
                is AuthResult.Failure -> {
                    _registerUiState.update {
                        it.copy(
                            masterPassword = "", // Limpieza de contraseña
                            confirmMasterPassword = "", // Limpieza de contraseña
                            isLoading = false,
                            generalErrorRes = R.string.register_error,
                        )
                    }
                }
            }
        }
    }

    /**
     * Factory manual para instanciar AuthViewModel inyectando el repositorio por constructor.
     */
    class Factory(
        private val repository: AuthRepository = FakeAuthRepository(),
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repository) as T
        }
    }
}
