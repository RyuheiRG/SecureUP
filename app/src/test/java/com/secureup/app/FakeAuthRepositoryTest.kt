package com.secureup.app

import com.secureup.app.data.repository.AuthResult
import com.secureup.app.data.repository.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeAuthRepositoryTest {

    private val repository = FakeAuthRepository(delayMs = 0L)

    @Test
    fun login_withValidCredentials_returnsSuccess() = runTest {
        val result = repository.login("usuario@test.com", "contrasenaSegura123")
        assertTrue(result is AuthResult.Success)
        val user = (result as AuthResult.Success).user
        assertEquals("usuario@test.com", user.email)
    }

    @Test
    fun login_withErrorEmail_returnsFailure() = runTest {
        val result = repository.login("error@test.com", "contrasenaSegura123")
        assertTrue(result is AuthResult.Failure)
    }

    @Test
    fun register_withValidCredentials_returnsSuccess() = runTest {
        val result = repository.register("nuevo@test.com", "contrasenaSegura123")
        assertTrue(result is AuthResult.Success)
        val user = (result as AuthResult.Success).user
        assertEquals("nuevo@test.com", user.email)
    }

    @Test
    fun register_withErrorEmail_returnsFailure() = runTest {
        val result = repository.register("error@test.com", "contrasenaSegura123")
        assertTrue(result is AuthResult.Failure)
    }
}
