package com.secureup.app.domain.repository

/**
 * Resultado de consultar una contraseña en el catálogo de brechas.
 */
sealed interface BreachResult {
    /** La contraseña no aparece en el catálogo consultado. */
    data object NotFound : BreachResult

    /** Número de veces que aparece la contraseña en las brechas conocidas. */
    data class Found(val occurrences: Long) : BreachResult

    /** No se pudo completar la consulta (red, servidor, formato). */
    data class Failure(val kind: BreachFailureKind) : BreachResult
}

enum class BreachFailureKind {
    NETWORK,
    SERVER,
    INVALID_RESPONSE,
}

/**
 * Verificación de contraseñas expuestas mediante k-anonimato.
 *
 * La contraseña completa nunca sale del dispositivo: solo se envía el prefijo
 * de 5 caracteres de su SHA-1 (ver contrato en `AGENTS.md` §7).
 */
interface BreachRepository {
    suspend fun checkPassword(password: String): BreachResult
}
