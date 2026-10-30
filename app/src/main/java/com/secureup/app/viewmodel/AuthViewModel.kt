package com.secureup.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.secureup.app.data.repository.AuthRepository
import com.secureup.app.data.repository.AuthResult
import com.secureup.app.data.repository.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la UI para la pantalla de Login.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
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
    val emailError: String? = null,
    val masterPasswordError: String? = null,
    val confirmMasterPasswordError: String? = null,
    val generalError: String? = null,
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
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _registerUiState = MutableStateFlow(RegisterUiState())
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

    fun onLoginEmailChanged(email: String) {
        _loginUiState.update {
            it.copy(
                email = email,
                emailError = null,
                generalError = null,
            )
        }
    }

    fun onLoginPasswordChanged(password: String) {
        _loginUiState.update {
            it.copy(
                password = password,
                passwordError = null,
                generalError = null,
            )
        }
    }

    fun login(onSuccess: () -> Unit = {}) {
        val currentState = _loginUiState.value
        val validation = AuthValidator.validateLogin(currentState.email, currentState.password)

        if (!validation.isValid) {
            _loginUiState.update {
                it.copy(
                    password = "", // Limpieza de contraseña por seguridad
                    emailError = validation.emailError,
                    passwordError = validation.passwordError,
                )
            }
            return
        }

        _loginUiState.update {
            it.copy(
                isLoading = true,
                emailError = null,
                passwordError = null,
                generalError = null,
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
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
                    onSuccess()
                }
                is AuthResult.Failure -> {
                    _loginUiState.update {
                        it.copy(
                            password = "", // Limpieza de contraseña por seguridad
                            isLoading = false,
                            generalError = "Credenciales incorrectas. Intenta de nuevo.",
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
                emailError = null,
                generalError = null,
            )
        }
    }

    fun onRegisterMasterPasswordChanged(password: String) {
        _registerUiState.update {
            it.copy(
                masterPassword = password,
                masterPasswordError = null,
                generalError = null,
            )
        }
    }

    fun onRegisterConfirmPasswordChanged(password: String) {
        _registerUiState.update {
            it.copy(
                confirmMasterPassword = password,
                confirmMasterPasswordError = null,
                generalError = null,
            )
        }
    }

    fun register(onSuccess: () -> Unit = {}) {
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
                    emailError = validation.emailError,
                    masterPasswordError = validation.passwordError,
                    confirmMasterPasswordError = validation.confirmPasswordError,
                )
            }
            return
        }

        _registerUiState.update {
            it.copy(
                isLoading = true,
                emailError = null,
                masterPasswordError = null,
                confirmMasterPasswordError = null,
                generalError = null,
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
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
                    onSuccess()
                }
                is AuthResult.Failure -> {
                    _registerUiState.update {
                        it.copy(
                            masterPassword = "", // Limpieza de contraseña
                            confirmMasterPassword = "", // Limpieza de contraseña
                            isLoading = false,
                            generalError = "Error al registrar la cuenta. Intenta de nuevo.",
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
