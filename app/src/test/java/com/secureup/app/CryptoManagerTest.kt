package com.secureup.app

import com.secureup.app.data.crypto.CryptoException
import com.secureup.app.data.crypto.CryptoManager
import com.secureup.app.data.crypto.EncryptedData
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del CryptoManager.
 *
 * Se usa una cuenta de iteraciones baja (1 000) para que las pruebas sean
 * rápidas; el valor de producción se verifica aparte en
 * [defaultIterations_matchOwaspRecommendation]. El algoritmo es idéntico en
 * ambos casos, solo cambia el coste.
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
        // Es la regla de AGENTS.md §6: auth_hash y clave de bóveda usan salts distintos,
        // así que la misma contraseña nunca produce la misma clave.
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
    fun deriveKey_withShortSalt_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            crypto.deriveKey(PASSWORD, ByteArray(8))
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

        val encrypted = crypto.encrypt(original, key)
        val decrypted = crypto.decryptAsString(encrypted, key)

        assertEquals(original, decrypted)
    }

    @Test
    fun encryptThenDecrypt_handlesEmptyAndBinaryPayloads() {
        val empty = crypto.encrypt(ByteArray(0), key)
        assertArrayEquals(ByteArray(0), crypto.decrypt(empty, key))

        val binary = ByteArray(256) { it.toByte() }
        val encrypted = crypto.encrypt(binary, key)
        assertArrayEquals(binary, crypto.decrypt(encrypted, key))
    }

    @Test
    fun encrypt_usesAFreshNoncePerOperation() {
        val plaintext = "misma contraseña, mismo key".toByteArray()
        val nonces = mutableSetOf<String>()
        val ciphertexts = mutableSetOf<String>()

        repeat(100) {
            val encrypted = crypto.encrypt(plaintext, key)
            nonces += encrypted.nonce.toHex()
            ciphertexts += encrypted.ciphertext.toHex()
            // Cada resultado debe seguir siendo descifrable con su propio nonce.
            assertArrayEquals(plaintext, crypto.decrypt(encrypted, key))
        }

        assertEquals("El nonce debe ser único por operación", 100, nonces.size)
        assertEquals("El ciphertext no debe repetirse", 100, ciphertexts.size)
        assertEquals(CryptoManager.NONCE_SIZE, crypto.encrypt(plaintext, key).nonce.size)
    }

    @Test
    fun decrypt_withTamperedTag_throws() {
        val encrypted = crypto.encrypt("datos sensibles", key)
        val tampered = encrypted.ciphertext.copyOf()
        tampered[tampered.lastIndex] = (tampered[tampered.lastIndex].toInt() xor 0x01).toByte()

        assertThrows(CryptoException::class.java) {
            crypto.decrypt(EncryptedData(tampered, encrypted.nonce), key)
        }
    }

    @Test
    fun decrypt_withTamperedPayloadByte_throws() {
        val encrypted = crypto.encrypt("datos sensibles", key)
        val tampered = encrypted.ciphertext.copyOf()
        tampered[0] = (tampered[0].toInt() xor 0x01).toByte()

        assertThrows(CryptoException::class.java) {
            crypto.decrypt(EncryptedData(tampered, encrypted.nonce), key)
        }
    }

    @Test
    fun decrypt_withTamperedNonce_throws() {
        val encrypted = crypto.encrypt("datos sensibles", key)
        val tamperedNonce = encrypted.nonce.copyOf()
        tamperedNonce[0] = (tamperedNonce[0].toInt() xor 0x01).toByte()

        assertThrows(CryptoException::class.java) {
            crypto.decrypt(EncryptedData(encrypted.ciphertext, tamperedNonce), key)
        }
    }

    @Test
    fun decrypt_withWrongKey_throws() {
        val otherSalt = ByteArray(CryptoManager.SALT_SIZE) { (it + 99).toByte() }
        val wrongKey = crypto.deriveKey(PASSWORD, otherSalt)
        val encrypted = crypto.encrypt("datos sensibles", key)

        assertThrows(CryptoException::class.java) {
            crypto.decrypt(encrypted, wrongKey)
        }
    }

    @Test
    fun decrypt_withInvalidNonceLength_throws() {
        val encrypted = crypto.encrypt("datos sensibles", key)

        assertThrows(CryptoException::class.java) {
            crypto.decrypt(EncryptedData(encrypted.ciphertext, ByteArray(8)), key)
        }
    }

    // --- Salt y datos auxiliares ---------------------------------------------

    @Test
    fun generateSalt_returnsRandomSaltsOfTheRequestedSize() {
        val first = crypto.generateSalt()
        val second = crypto.generateSalt()

        assertEquals(CryptoManager.SALT_SIZE, first.size)
        assertFalse(first.contentEquals(second))
    }

    @Test
    fun encryptedData_equalsComparesContents() {
        val ciphertext = "cipher".toByteArray()
        val nonce = ByteArray(CryptoManager.NONCE_SIZE)

        assertEquals(EncryptedData(ciphertext, nonce), EncryptedData(ciphertext.copyOf(), nonce.copyOf()))
        assertNotEquals(EncryptedData(ciphertext, nonce), EncryptedData("otro".toByteArray(), nonce))
    }

    @Test
    fun encryptedData_toStringDoesNotExposeBytes() {
        val encrypted = crypto.encrypt("datos sensibles", key)

        val text = encrypted.toString()

        assertFalse(text.contains(encrypted.ciphertext.toHex()))
        assertFalse(text.contains("datos sensibles"))
    }

    private companion object {
        private const val TEST_ITERATIONS = 1_000
        private val PASSWORD = "contrasenaMaestraSegura123".toCharArray()
    }
}

private fun ByteArray.toHex(): String = joinToString(separator = "") { byte -> "%02x".format(byte) }
