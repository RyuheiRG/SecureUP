package com.secureup.app.data.repository

import com.secureup.app.model.User
import kotlinx.coroutines.delay

/**
 * Repositorio de autenticación simulado en memoria.
 * SOLO PARA DESARROLLO Y PRUEBAS.
 *
 * Acepta cualquier correo válido con contraseña de al menos 12 caracteres,
 * excepto "error@test.com", que siempre falla para probar casos de error.
 */
class FakeAuthRepository(
    private val delayMs: Long = 800L,
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResult {
        if (delayMs > 0) delay(delayMs)

        val cleanEmail = email.trim()
        if (cleanEmail.equals("error@test.com", ignoreCase = true)) {
            return AuthResult.Failure("Credenciales incorrectas")
        }

        if (cleanEmail.contains("@") && password.length >= 12) {
            return AuthResult.Success(
                user = User(
                    id = "fake-user-id-123",
                    email = cleanEmail,
                )
            )
        }

        return AuthResult.Failure("Credenciales incorrectas")
    }

    override suspend fun register(email: String, password: String): AuthResult {
        if (delayMs > 0) delay(delayMs)

        val cleanEmail = email.trim()
        if (cleanEmail.equals("error@test.com", ignoreCase = true)) {
            return AuthResult.Failure("El correo electrónico ya se encuentra registrado")
        }

        if (cleanEmail.contains("@") && password.length >= 12) {
            return AuthResult.Success(
                user = User(
                    id = "fake-user-id-123",
                    email = cleanEmail,
                )
            )
        }

        return AuthResult.Failure("Error al registrar cuenta")
    }
}
