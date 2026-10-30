package com.secureup.app.data.repository

import com.secureup.app.model.User

/**
 * Resultado de las operaciones de autenticación.
 */
sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data class Failure(val reason: String) : AuthResult
}

/**
 * Interfaz para el repositorio de autenticación.
 */
interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult
    suspend fun register(email: String, password: String): AuthResult
}
