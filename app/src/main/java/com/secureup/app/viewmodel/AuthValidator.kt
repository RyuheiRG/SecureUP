package com.secureup.app.viewmodel

import androidx.annotation.StringRes
import com.secureup.app.R

/**
 * Resultado de la validación de campos de autenticación.
 *
 * Los errores son IDs de `strings.xml` (@StringRes), no texto: el ViewModel no
 * conoce el idioma ni tiene acceso a recursos, y la pantalla los resuelve con
 * `stringResource`.
 */
data class AuthValidationResult(
    val isValid: Boolean,
    @get:StringRes val emailErrorRes: Int? = null,
    @get:StringRes val passwordErrorRes: Int? = null,
    @get:StringRes val confirmPasswordErrorRes: Int? = null,
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
        val emailErrorRes = when {
            sanitizedEmail.isBlank() -> R.string.auth_error_email_required_login
            !EMAIL_REGEX.matches(sanitizedEmail) -> R.string.auth_error_email_invalid
            else -> null
        }

        val passwordErrorRes = when {
            password.isEmpty() -> R.string.auth_error_password_required
            password.length < 12 -> R.string.auth_error_password_too_short
            else -> null
        }

        return AuthValidationResult(
            isValid = emailErrorRes == null && passwordErrorRes == null,
            emailErrorRes = emailErrorRes,
            passwordErrorRes = passwordErrorRes,
            sanitizedEmail = sanitizedEmail,
        )
    }

    fun validateRegister(email: String, password: String, confirmPassword: String): AuthValidationResult {
        val sanitizedEmail = email.trim()
        val emailErrorRes = when {
            sanitizedEmail.isBlank() -> R.string.auth_error_email_required_register
            !EMAIL_REGEX.matches(sanitizedEmail) -> R.string.auth_error_email_invalid
            else -> null
        }

        val passwordErrorRes = when {
            password.isEmpty() -> R.string.auth_error_password_required
            password.length < 12 -> R.string.auth_error_password_too_short
            else -> null
        }

        val confirmPasswordErrorRes = when {
            confirmPassword.isEmpty() -> R.string.auth_error_confirm_required
            confirmPassword != password -> R.string.auth_error_confirm_mismatch
            else -> null
        }

        return AuthValidationResult(
            isValid = emailErrorRes == null && passwordErrorRes == null && confirmPasswordErrorRes == null,
            emailErrorRes = emailErrorRes,
            passwordErrorRes = passwordErrorRes,
            confirmPasswordErrorRes = confirmPasswordErrorRes,
            sanitizedEmail = sanitizedEmail,
        )
    }
}
