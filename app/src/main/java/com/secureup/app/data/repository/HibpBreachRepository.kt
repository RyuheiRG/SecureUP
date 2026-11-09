package com.secureup.app.data.repository

import com.secureup.app.data.remote.HibpApi
import com.secureup.app.domain.repository.BreachFailureKind
import com.secureup.app.domain.repository.BreachRepository
import com.secureup.app.domain.repository.BreachResult
import java.io.IOException
import java.security.MessageDigest
import java.text.Normalizer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Verificación real contra Pwned Passwords de HIBP.
 *
 * Flujo:
 * 1. Normaliza la contraseña a NFC (la misma "ñ" puede llegar de dos formas).
 * 2. Calcula su SHA-1 y separa prefijo (5 hex) y sufijo (35 hex).
 * 3. Envía SOLO el prefijo; la API devuelve todos los sufijos que empiezan así.
 * 4. Busca el sufijo localmente y devuelve el contador.
 *
 * La contraseña completa y el hash completo nunca salen del dispositivo ni se
 * escriben en logs.
 */
class HibpBreachRepository(
    private val api: HibpApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BreachRepository {

    override suspend fun checkPassword(password: String): BreachResult =
        withContext(ioDispatcher) {
            if (password.isEmpty()) {
                return@withContext BreachResult.Failure(BreachFailureKind.INVALID_RESPONSE)
            }

            val normalized = Normalizer.normalize(password, Normalizer.Form.NFC)
            val hash = sha1Hex(normalized.toByteArray(Charsets.UTF_8))
            val prefix = hash.substring(0, PREFIX_LENGTH)
            val suffix = hash.substring(PREFIX_LENGTH)

            try {
                val response = api.getRange(prefix)
                if (!response.isSuccessful) {
                    val kind = if (response.code() >= 500) {
                        BreachFailureKind.SERVER
                    } else {
                        BreachFailureKind.NETWORK
                    }
                    return@withContext BreachResult.Failure(kind)
                }
                val body = response.body()
                    ?: return@withContext BreachResult.Failure(BreachFailureKind.INVALID_RESPONSE)
                parseOccurrences(body, suffix)
            } catch (e: IOException) {
                BreachResult.Failure(BreachFailureKind.NETWORK)
            } catch (e: IllegalArgumentException) {
                BreachResult.Failure(BreachFailureKind.INVALID_RESPONSE)
            }
        }

    /**
     * Busca el sufijo en la respuesta `SUFFIX:COUNT` por línea. Las filas de
     * relleno traen contador 0 y se tratan como "no encontrada".
     */
    private fun parseOccurrences(body: String, suffix: String): BreachResult {
        for (line in body.lineSequence()) {
            val cleaned = line.trim()
            if (cleaned.isEmpty()) continue
            val separator = cleaned.indexOf(':')
            if (separator <= 0) continue
            val candidate = cleaned.substring(0, separator)
            if (!candidate.equals(suffix, ignoreCase = true)) continue
            val count = cleaned.substring(separator + 1).trim().toLongOrNull()
                ?: return BreachResult.Failure(BreachFailureKind.INVALID_RESPONSE)
            return if (count > 0) {
                BreachResult.Found(count)
            } else {
                BreachResult.NotFound
            }
        }
        return BreachResult.NotFound
    }

    private fun sha1Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(bytes)
        val hex = StringBuilder(digest.size * 2)
        for (byte in digest) {
            val value = byte.toInt() and 0xFF
            hex.append(HEX_CHARS[value ushr 4])
            hex.append(HEX_CHARS[value and 0x0F])
        }
        return hex.toString().uppercase()
    }

    private companion object {
        const val PREFIX_LENGTH = 5
        val HEX_CHARS = "0123456789ABCDEF".toCharArray()
    }
}
