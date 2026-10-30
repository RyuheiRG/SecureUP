package com.secureup.app.viewmodel

/**
 * Resultado de la validación de campos de autenticación.
 */
data class AuthValidationResult(
    val isValid: Boolean,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val sanitizedEmail: String = "",
)

/**
 * Validador puro para el formulario de Autenticación (Login y Registro).
 *
 * Reglas:
 * - Recorta espacios (trim) ÚNICAMENTE en el correo electrónico.
 * - Verifica formato válido de correo mediante expresión regular (sin dependencias de Android).
 * - La contraseña maestra no lleva trim y requiere mínimo 12 caracteres.
 * - La confirmación de contraseña debe ser exactamente igual a la contraseña.
 */
object AuthValidator {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateLogin(email: String, password: String): AuthValidationResult {
        val sanitizedEmail = email.trim()
        val emailError = when {
            sanitizedEmail.isBlank() -> "Ingresa tu correo electrónico"
            !EMAIL_REGEX.matches(sanitizedEmail) -> "Formato de correo electrónico inválido"
            else -> null
        }

        val passwordError = when {
            password.isEmpty() -> "Ingresa tu contraseña maestra"
            password.length < 12 -> "La contraseña maestra debe tener al menos 12 caracteres"
            else -> null
        }

        return AuthValidationResult(
            isValid = emailError == null && passwordError == null,
            emailError = emailError,
            passwordError = passwordError,
            sanitizedEmail = sanitizedEmail,
        )
    }

    fun validateRegister(email: String, password: String, confirmPassword: String): AuthValidationResult {
        val sanitizedEmail = email.trim()
        val emailError = when {
            sanitizedEmail.isBlank() -> "Ingresa un correo electrónico"
            !EMAIL_REGEX.matches(sanitizedEmail) -> "Formato de correo electrónico inválido"
            else -> null
        }

        val passwordError = when {
            password.isEmpty() -> "Ingresa tu contraseña maestra"
            password.length < 12 -> "La contraseña maestra debe tener al menos 12 caracteres"
            else -> null
        }

        val confirmPasswordError = when {
            confirmPassword.isEmpty() -> "Confirma tu contraseña maestra"
            confirmPassword != password -> "Las contraseñas no coinciden"
            else -> null
        }

        return AuthValidationResult(
            isValid = emailError == null && passwordError == null && confirmPasswordError == null,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            sanitizedEmail = sanitizedEmail,
        )
    }
}
