package com.secureup.app

import com.secureup.app.data.crypto.CryptoException
import com.secureup.app.data.crypto.CryptoManager
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del CryptoManager.
 *
 * Se usa una cuenta de iteraciones baja (1 000) para que las pruebas sean
 * rápidas; el valor de producción se verifica aparte en
 * [defaultIterations_matchOwaspRecommendation].
 */
class CryptoManagerTest {

    private val crypto = CryptoManager(defaultIterations = TEST_ITERATIONS)
    private val salt = ByteArray(CryptoManager.SALT_SIZE) { (it + 1).toByte() }
    private val key = crypto.deriveKey(PASSWORD, salt)

    // --- Derivación de claves -------------------------------------------------

    @Test
    fun deriveKey_isDeterministicForSamePasswordAndSalt() {
        val first = crypto.deriveKey(PASSWORD, salt)
        val second = crypto.deriveKey(PASSWORD, salt)

        assertArrayEquals(first.encoded, second.encoded)
    }

    @Test
    fun deriveKey_producesA256BitKey() {
        assertEquals(CryptoManager.KEY_SIZE_BITS / 8, key.encoded.size)
    }

    @Test
    fun deriveKey_withDifferentSalt_producesDifferentKey() {
        val otherSalt = ByteArray(CryptoManager.SALT_SIZE) { (it + 99).toByte() }
        val otherKey = crypto.deriveKey(PASSWORD, otherSalt)

        assertFalse(key.encoded.contentEquals(otherKey.encoded))
    }

    @Test
    fun deriveKey_withDifferentIterations_producesDifferentKey() {
        val otherKey = crypto.deriveKey(PASSWORD, salt, iterations = TEST_ITERATIONS + 1)

        assertFalse(key.encoded.contentEquals(otherKey.encoded))
    }

    @Test
    fun deriveKey_withEmptySalt_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            crypto.deriveKey(PASSWORD, ByteArray(0))
        }
    }

    @Test
    fun defaultIterations_matchOwaspRecommendation() {
        assertTrue(
            "OWASP exige al menos 600 000 iteraciones para PBKDF2-HMAC-SHA256",
            CryptoManager.DEFAULT_ITERATIONS >= 600_000,
        )
    }

    // --- Cifrado / descifrado -------------------------------------------------

    @Test
    fun encryptThenDecrypt_returnsOriginalText() {
        val original = "contraseña con acentos y símbolos: ñ@€"

        val blob = crypto.encrypt(original, key)

        assertEquals(original, crypto.decryptAsString(blob, key))
    }

    @Test
    fun encryptThenDecrypt_handlesEmptyAndBinaryPayloads() {
        val empty = crypto.encrypt(ByteArray(0), key)
        assertArrayEquals(ByteArray(0), crypto.decrypt(empty, key))

        val binary = ByteArray(256) { it.toByte() }
        val blob = crypto.encrypt(binary, key)
        assertArrayEquals(binary, crypto.decrypt(blob, key))
    }

    @Test
    fun blob_hasVersionByteAndNonce() {
        val blob = crypto.encrypt("datos", key)

        assertEquals(CryptoManager.BLOB_VERSION, blob[0].toInt())
        assertEquals(CryptoManager.MIN_BLOB_SIZE + "datos".length, blob.size)
    }

    @Test
    fun encrypt_usesAFreshNoncePerOperation() {
        val plaintext = "misma contraseña, misma key".toByteArray()
        val nonces = mutableSetOf<String>()
        val blobs = mutableSetOf<String>()

        repeat(100) {
            val blob = crypto.encrypt(plaintext, key)
            nonces += blob.nonceOf().toHex()
            blobs += blob.toHex()
            // Cada resultado debe seguir siendo descifrable con su propio nonce.
            assertArrayEquals(plaintext, crypto.decrypt(blob, key))
        }

        assertEquals("El nonce debe ser único por operación", 100, nonces.size)
        assertEquals("El blob no debe repetirse", 100, blobs.size)
    }

    @Test
    fun decrypt_withTamperedTag_throws() {
        val blob = crypto.encrypt("datos sensibles", key)
        blob[blob.lastIndex] = (blob[blob.lastIndex].toInt() xor 0x01).toByte()

        assertThrows(CryptoException::class.java) { crypto.decrypt(blob, key) }
    }

    @Test
    fun decrypt_withTamperedPayloadByte_throws() {
        val blob = crypto.encrypt("datos sensibles", key)
        blob[CryptoManager.NONCE_SIZE + 1] = (blob[CryptoManager.NONCE_SIZE + 1].toInt() xor 0x01).toByte()

        assertThrows(CryptoException::class.java) { crypto.decrypt(blob, key) }
    }

    @Test
    fun decrypt_withTamperedNonce_throws() {
        val blob = crypto.encrypt("datos sensibles", key)
        blob[1] = (blob[1].toInt() xor 0x01).toByte()

        assertThrows(CryptoException::class.java) { crypto.decrypt(blob, key) }
    }

    @Test
    fun decrypt_withUnknownVersion_throws() {
        val blob = crypto.encrypt("datos sensibles", key)
        blob[0] = 0x02

        assertThrows(CryptoException::class.java) { crypto.decrypt(blob, key) }
    }

    @Test
    fun decrypt_withTruncatedBlob_throws() {
        val blob = crypto.encrypt("datos sensibles", key)

        assertThrows(CryptoException::class.java) {
            crypto.decrypt(blob.copyOf(CryptoManager.MIN_BLOB_SIZE - 1), key)
        }
        assertThrows(CryptoException::class.java) {
            crypto.decrypt(ByteArray(0), key)
        }
    }

    @Test
    fun decrypt_withWrongKey_throws() {
        val otherSalt = ByteArray(CryptoManager.SALT_SIZE) { (it + 99).toByte() }
        val wrongKey = crypto.deriveKey(PASSWORD, otherSalt)
        val blob = crypto.encrypt("datos sensibles", key)

        assertThrows(CryptoException::class.java) { crypto.decrypt(blob, wrongKey) }
    }

    @Test
    fun decrypt_withDifferentAad_throws() {
        val entryId = "entrada-1".toByteArray()
        val blob = crypto.encrypt("datos sensibles", key, aad = entryId)

        // Con el AAD correcto descifra...
        assertEquals("datos sensibles", crypto.decryptAsString(blob, key, aad = entryId))
        // ...con otro AAD (blob intercambiado entre entradas) falla.
        assertThrows(CryptoException::class.java) {
            crypto.decrypt(blob, key, aad = "entrada-2".toByteArray())
        }
        assertThrows(CryptoException::class.java) {
            crypto.decrypt(blob, key)
        }
    }

    // --- Salt ----------------------------------------------------------------

    @Test
    fun generateSalt_returnsRandomSaltsOfTheExpectedSize() {
        val first = crypto.generateSalt()
        val second = crypto.generateSalt()

        assertEquals(CryptoManager.SALT_SIZE, first.size)
        assertFalse(first.contentEquals(second))
    }

    private companion object {
        private const val TEST_ITERATIONS = 1_000
        private val PASSWORD = "contrasenaMaestraSegura123".toCharArray()
    }
}

internal fun ByteArray.nonceOf(): ByteArray = copyOfRange(1, 1 + CryptoManager.NONCE_SIZE)

internal fun ByteArray.toHex(): String = joinToString(separator = "") { byte -> "%02x".format(byte) }
