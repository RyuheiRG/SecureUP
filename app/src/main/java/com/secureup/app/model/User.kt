package com.secureup.app.model

/**
 * Modelo de dominio que representa la información del perfil de un usuario.
 */
data class User(
    val id: String = "1",
    val name: String = "Usuario",
    val email: String = "usuario@email.com",
    val lastLogin: String = "28 sep 2026, 09:15",
)
