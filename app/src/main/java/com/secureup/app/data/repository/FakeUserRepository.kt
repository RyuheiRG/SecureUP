package com.secureup.app.data.repository

import com.secureup.app.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementación de prueba (Fake) de UserRepository con retardos simulados.
 * Permite probar la interfaz de usuario sin necesidad de backend o base de datos real.
 */
class FakeUserRepository : UserRepository {

    private val _userState = MutableStateFlow(
        User(
            id = "user_1",
            name = "Usuario",
            email = "usuario@email.com",
            lastLogin = "28 sep 2026, 09:15",
        ),
    )

    override fun getUser(): Flow<User> = _userState.asStateFlow()

    override suspend fun updateName(name: String): Result<User> {
        delay(1000) // Simulación de llamada a red
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("El nombre no puede estar vacío."))
        }
        if (trimmed.length > 50) {
            return Result.failure(IllegalArgumentException("El nombre no puede exceder los 50 caracteres."))
        }

        val updatedUser = _userState.value.copy(name = trimmed)
        _userState.value = updatedUser
        return Result.success(updatedUser)
    }

    override suspend fun changeMasterPassword(currentPassword: String, newPassword: String): Result<Unit> {
        delay(1500) // Simulación de derivación de claves y re-cifrado de la bóveda
        if (currentPassword.isEmpty()) {
            return Result.failure(IllegalArgumentException("Ingresa la contraseña actual."))
        }
        if (currentPassword == "12345" || currentPassword == "incorrecta") {
            return Result.failure(IllegalArgumentException("Contraseña maestra actual incorrecta."))
        }
        if (newPassword == currentPassword) {
            return Result.failure(IllegalArgumentException("La nueva contraseña debe ser diferente a la actual."))
        }
        return Result.success(Unit)
    }

    override suspend fun logout(): Result<Unit> {
        delay(500)
        _userState.value = User()
        return Result.success(Unit)
    }
}
