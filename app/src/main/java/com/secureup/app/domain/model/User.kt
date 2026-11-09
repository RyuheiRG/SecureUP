package com.secureup.app.domain.model

/**
 * Modelo de dominio para representar al usuario autenticado.
 */
data class User(
    val id: String,
    val email: String,
)
