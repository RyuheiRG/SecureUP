package com.secureup.app

import com.secureup.app.viewmodel.AuthValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun validateLogin_withInvalidEmail_returnsEmailError() {
        val result = AuthValidator.validateLogin("correo-invalido", "contrasenaSegura123")
        assertFalse(result.isValid)
        assertEquals(R.string.auth_error_email_invalid, result.emailErrorRes)
        assertNull(result.passwordErrorRes)
    }

    @Test
    fun validateLogin_withShortPassword_returnsPasswordError() {
        val result = AuthValidator.validateLogin("usuario@correo.com", "corta123")
        assertFalse(result.isValid)
        assertNull(result.emailErrorRes)
        assertEquals(R.string.auth_error_password_too_short, result.passwordErrorRes)
    }

    @Test
    fun validateLogin_withValidCredentials_returnsValid() {
        val result = AuthValidator.validateLogin("  usuario@correo.com  ", "contrasenaSegura123")
        assertTrue(result.isValid)
        assertNull(result.emailErrorRes)
        assertNull(result.passwordErrorRes)
        assertEquals("usuario@correo.com", result.sanitizedEmail)
    }

    @Test
    fun validateRegister_withPasswordMismatch_returnsConfirmPasswordError() {
        val result = AuthValidator.validateRegister(
            email = "usuario@correo.com",
            password = "contrasenaSegura123",
            confirmPassword = "contrasenaDiferente123",
        )
        assertFalse(result.isValid)
        assertNull(result.emailErrorRes)
        assertNull(result.passwordErrorRes)
        assertEquals(R.string.auth_error_confirm_mismatch, result.confirmPasswordErrorRes)
    }

    @Test
    fun validateRegister_withValidInput_returnsValid() {
        val result = AuthValidator.validateRegister(
            email = "  usuario@correo.com  ",
            password = "contrasenaSegura123",
            confirmPassword = "contrasenaSegura123",
        )
        assertTrue(result.isValid)
        assertNull(result.emailErrorRes)
        assertNull(result.passwordErrorRes)
        assertNull(result.confirmPasswordErrorRes)
        assertEquals("usuario@correo.com", result.sanitizedEmail)
    }

    @Test
    fun validateRegister_withBlankEmail_usesRegisterSpecificMessage() {
        val loginResult = AuthValidator.validateLogin("", "contrasenaSegura123")
        val registerResult = AuthValidator.validateRegister("", "contrasenaSegura123", "contrasenaSegura123")

        assertEquals(R.string.auth_error_email_required_login, loginResult.emailErrorRes)
        assertEquals(R.string.auth_error_email_required_register, registerResult.emailErrorRes)
    }
}
