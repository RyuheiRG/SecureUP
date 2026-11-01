package com.secureup.app.model

/**
 * Modelo de dominio para representar al usuario autenticado.
 */
data class User(
    val id: String = "1",
    val email: String = "usuario@email.com",
    val name: String = "Usuario",
    val lastLogin: String = "28 sep 2026, 09:15",
)
