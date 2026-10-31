package com.secureup.app.data.repository

import com.secureup.app.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de repositorio para operaciones relacionadas con el usuario y su cuenta.
 */
interface UserRepository {
    /** Obtenemos un flujo con el usuario actual. */
    fun getUser(): Flow<User>

    /** Actualiza el nombre del usuario. */
    suspend fun updateName(name: String): Result<User>

    /** Cambia la contraseña maestra realizando re-cifrado. */
    suspend fun changeMasterPassword(currentPassword: String, newPassword: String): Result<Unit>

    /** Cierra la sesión y destruye datos sensibles de la memoria. */
    suspend fun logout(): Result<Unit>
}
