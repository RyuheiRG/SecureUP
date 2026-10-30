package com.secureup.app

import com.secureup.app.viewmodel.AuthValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun validateLogin_withInvalidEmail_returnsEmailError() {
        val result = AuthValidator.validateLogin("correo-invalido", "contrasenaSegura123")
        assertFalse(result.isValid)
        assertNotNull(result.emailError)
        assertNull(result.passwordError)
    }

    @Test
    fun validateLogin_withShortPassword_returnsPasswordError() {
        val result = AuthValidator.validateLogin("usuario@correo.com", "corta123")
        assertFalse(result.isValid)
        assertNull(result.emailError)
        assertNotNull(result.passwordError)
    }

    @Test
    fun validateLogin_withValidCredentials_returnsValid() {
        val result = AuthValidator.validateLogin("  usuario@correo.com  ", "contrasenaSegura123")
        assertTrue(result.isValid)
        assertNull(result.emailError)
        assertNull(result.passwordError)
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
        assertNull(result.emailError)
        assertNull(result.passwordError)
        assertNotNull(result.confirmPasswordError)
    }

    @Test
    fun validateRegister_withValidInput_returnsValid() {
        val result = AuthValidator.validateRegister(
            email = "  usuario@correo.com  ",
            password = "contrasenaSegura123",
            confirmPassword = "contrasenaSegura123",
        )
        assertTrue(result.isValid)
        assertNull(result.emailError)
        assertNull(result.passwordError)
        assertNull(result.confirmPasswordError)
        assertEquals("usuario@correo.com", result.sanitizedEmail)
    }
}
