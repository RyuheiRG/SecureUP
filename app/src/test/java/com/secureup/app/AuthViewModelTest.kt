package com.secureup.app

import com.secureup.app.data.repository.AuthRepository
import com.secureup.app.data.repository.AuthResult
import com.secureup.app.model.User
import com.secureup.app.viewmodel.AuthViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Pruebas del AuthViewModel: validación, estado de carga, mensajes genéricos y
 * limpieza de contraseñas. El repositorio está doblado (se registra si se llamó).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private val repository = RecordingAuthRepository()
    private val viewModel = AuthViewModel(repository, dispatcher)

    @Test
    fun login_withInvalidEmail_doesNotCallRepository_andClearsPassword() = runTest(dispatcher) {
        viewModel.onLoginEmailChanged("correo-invalido")
        viewModel.onLoginPasswordChanged("contrasenaSegura123")

        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.loginUiState.value
        assertEquals(0, repository.loginCalls)
        assertEquals(R.string.auth_error_email_invalid, state.emailErrorRes)
        assertNull(state.passwordErrorRes)
        assertEquals("", state.password)
        assertFalse(state.isAuthenticated)
    }

    @Test
    fun login_withValidCredentials_setsLoadingThenAuthenticates_andInvokesCallback() = runTest(dispatcher) {
        var callbackCalled = false
        viewModel.onLoginEmailChanged("  usuario@correo.com  ")
        viewModel.onLoginPasswordChanged("contrasenaSegura123")

        viewModel.login(onSuccess = { callbackCalled = true })

        // Aún no se avanzó el dispatcher: el estado intermedio debe ser "cargando".
        assertTrue(viewModel.loginUiState.value.isLoading)

        advanceUntilIdle()

        val state = viewModel.loginUiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isAuthenticated)
        assertEquals("", state.password)
        assertTrue(callbackCalled)
        assertEquals(1, repository.loginCalls)
        assertEquals("usuario@correo.com", repository.lastLoginEmail)
    }

    @Test
    fun login_withRepositoryFailure_showsGenericMessage_andClearsPassword() = runTest(dispatcher) {
        repository.loginResult = AuthResult.Failure("motivo interno que no debe mostrarse")
        viewModel.onLoginEmailChanged("usuario@correo.com")
        viewModel.onLoginPasswordChanged("contrasenaSegura123")

        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.loginUiState.value
        assertEquals(R.string.login_error, state.generalErrorRes)
        assertEquals("", state.password)
        assertFalse(state.isAuthenticated)
    }

    @Test
    fun register_withPasswordMismatch_doesNotCallRepository_andClearsPasswords() = runTest(dispatcher) {
        viewModel.onRegisterEmailChanged("usuario@correo.com")
        viewModel.onRegisterMasterPasswordChanged("contrasenaSegura123")
        viewModel.onRegisterConfirmPasswordChanged("otraContrasena123")

        viewModel.register()
        advanceUntilIdle()

        val state = viewModel.registerUiState.value
        assertEquals(0, repository.registerCalls)
        assertEquals(R.string.auth_error_confirm_mismatch, state.confirmMasterPasswordErrorRes)
        assertEquals("", state.masterPassword)
        assertEquals("", state.confirmMasterPassword)
        assertFalse(state.isAuthenticated)
    }

    @Test
    fun register_withValidInput_authenticatesAndClearsPasswords() = runTest(dispatcher) {
        viewModel.onRegisterEmailChanged("usuario@correo.com")
        viewModel.onRegisterMasterPasswordChanged("contrasenaSegura123")
        viewModel.onRegisterConfirmPasswordChanged("contrasenaSegura123")

        viewModel.register()
        advanceUntilIdle()

        val state = viewModel.registerUiState.value
        assertEquals(1, repository.registerCalls)
        assertTrue(state.isAuthenticated)
        assertEquals("", state.masterPassword)
        assertEquals("", state.confirmMasterPassword)
    }

    /**
     * Repositorio doble: devuelve lo que la prueba configure y cuenta las llamadas.
     */
    private class RecordingAuthRepository : AuthRepository {

        var loginResult: AuthResult =
            AuthResult.Success(User(id = "user-id", email = "usuario@correo.com"))
        var registerResult: AuthResult =
            AuthResult.Success(User(id = "user-id", email = "usuario@correo.com"))

        var loginCalls = 0
        var registerCalls = 0
        var lastLoginEmail: String? = null
        var lastRegisterEmail: String? = null

        override suspend fun login(email: String, password: String): AuthResult {
            loginCalls++
            lastLoginEmail = email
            return loginResult
        }

        override suspend fun register(email: String, password: String): AuthResult {
            registerCalls++
            lastRegisterEmail = email
            return registerResult
        }
    }
}
