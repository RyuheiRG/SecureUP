package com.secureup.app

import com.secureup.app.data.remote.HibpApi
import com.secureup.app.data.repository.HibpBreachRepository
import com.secureup.app.domain.repository.BreachFailureKind
import com.secureup.app.domain.repository.BreachResult
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * Pruebas del cliente de HIBP. Verifican el k-anonimato (solo viaja el prefijo
 * de 5 caracteres), el parseo de la respuesta y la normalización NFC.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HibpBreachRepositoryTest {

    private val api = FakeHibpApi()
    private val repository = HibpBreachRepository(api, UnconfinedTestDispatcher())

    @Test
    fun knownPassword_returnsOccurrences() = runTest {
        // SHA-1("password") = 5BAA61E4C9B93F3F0682250B6CF8331B7EE68FD8
        api.body = "1E4C9B93F3F0682250B6CF8331B7EE68FD8:12345\r\n" +
            "00000000000000000000000000000000000:0\r\n"

        val result = repository.checkPassword("password")

        assertEquals(BreachResult.Found(12345), result)
        assertEquals("5BAA6", api.lastPrefix)
    }

    @Test
    fun paddedZeroCount_isNotFound() = runTest {
        // El sufijo aparece con contador 0 porque Add-Padding rellena la respuesta.
        api.body = "1E4C9B93F3F0682250B6CF8331B7EE68FD8:0\r\n"

        val result = repository.checkPassword("password")

        assertEquals(BreachResult.NotFound, result)
    }

    @Test
    fun missingSuffix_isNotFound() = runTest {
        api.body = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA:5\r\n"

        val result = repository.checkPassword("password")

        assertEquals(BreachResult.NotFound, result)
    }

    @Test
    fun emptyBody_isNotFound() = runTest {
        api.body = ""

        val result = repository.checkPassword("password")

        assertEquals(BreachResult.NotFound, result)
    }

    @Test
    fun ioException_returnsNetworkFailure() = runTest {
        api.exception = IOException("sin red")

        val result = repository.checkPassword("password")

        assertEquals(BreachResult.Failure(BreachFailureKind.NETWORK), result)
    }

    @Test
    fun emptyPassword_isRejectedWithoutCallingApi() = runTest {
        val result = repository.checkPassword("")

        assertEquals(BreachResult.Failure(BreachFailureKind.INVALID_RESPONSE), result)
        assertEquals(null, api.lastPrefix)
    }

    @Test
    fun nfcComposedAndDecomposedPasswords_sendTheSamePrefix() = runTest {
        api.body = ""
        repository.checkPassword("contrase\u00F1a")
        val composedPrefix = api.lastPrefix

        repository.checkPassword("contrasen\u0303a")
        val decomposedPrefix = api.lastPrefix

        assertEquals(composedPrefix, decomposedPrefix)
        assertTrue(composedPrefix!!.length == 5)
    }

    private class FakeHibpApi : HibpApi {
        var body: String = ""
        var exception: IOException? = null
        var lastPrefix: String? = null

        override suspend fun getRange(prefix: String): Response<String> {
            lastPrefix = prefix
            exception?.let { throw it }
            return Response.success(body)
        }
    }
}
