package com.secureup.app

import com.secureup.app.data.crypto.CryptoException
import com.secureup.app.data.crypto.CryptoManager
import com.secureup.app.data.crypto.KdfParams
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del CryptoManager.
 *
 * Se construye con `minIterations = 1` y se derivan con pocas iteraciones para
 * que las pruebas sean rápidas; los valores de producción se verifican aparte en
 * [minAcceptedIterations_matchOwaspRecommendation].
 */
class CryptoManagerTest {

    private val crypto = CryptoManager(minIterations = 1)
    private val saltAuth = "salt-auth".toByteArray()
    private val saltVault = "salt-vault".toByteArray()
    private val keys = crypto.deriveKeys(
        masterPassword = PASSWORD,
        saltAuth = saltAuth,
        saltVault = saltVault,
        params = KdfParams(iterations = TEST_ITERATIONS),
    )
    private val key = keys.vaultKey

    // --- Derivación de claves -------------------------------------------------

    @Test
    fun deriveKeys_isDeterministicForSamePasswordAndSalts() {
        val first = deriveTestKeys()
        val second = deriveTestKeys()

        assertArrayEquals(first.authHash, second.authHash)
        assertArrayEquals(first.vaultKey.encoded, second.vaultKey.encoded)
    }

    @Test
    fun deriveKeys_produces32ByteValues() {
        assertEquals(CryptoManager.KEY_SIZE_BITS / 8, keys.authHash.size)
        assertEquals(CryptoManager.KEY_SIZE_BITS / 8, keys.vaultKey.encoded.size)
    }

    @Test
    fun deriveKeys_authHashDiffersFromVaultKey() {
        // Si alguien cambia un salt por el otro, esto falla.
        assertFalse(keys.authHash.contentEquals(keys.vaultKey.encoded))
    }

    @Test
    fun deriveKeys_changingSaltVaultChangesOnlyTheVaultKey() {
        val otherKeys = crypto.deriveKeys(
            masterPassword = PASSWORD,
            saltAuth = saltAuth,
            saltVault = "otro-salt-vault".toByteArray(),
            params = KdfParams(iterations = TEST_ITERATIONS),
        )

        assertArrayEquals("authHash depende solo de saltAuth", keys.authHash, otherKeys.authHash)
        assertFalse(
            "vaultKey debe cambiar al cambiar saltVault",
            keys.vaultKey.encoded.contentEquals(otherKeys.vaultKey.encoded),
        )
    }

    @Test
    fun deriveKeys_changingIterationsChangesBothKeys() {
        val otherKeys = deriveTestKeys(iterations = TEST_ITERATIONS + 1)

        assertFalse(keys.authHash.contentEquals(otherKeys.authHash))
        assertFalse(keys.vaultKey.encoded.contentEquals(otherKeys.vaultKey.encoded))
    }

    @Test
    fun deriveKeys_acceptsShortSalts() {
        // Los vectores del RFC usan salts de 4 bytes; la longitud mínima de 16 se
        // exige al generar el salt, no al derivar con uno que venga del servidor.
        val result = crypto.deriveKeys(PASSWORD, "salt".toByteArray(), "nacl".toByteArray(), KdfParams(iterations = 1))

        assertEquals(32, result.authHash.size)
    }

    @Test
    fun deriveKeys_rejectsEqualSalts() {
        assertThrows(IllegalArgumentException::class.java) {
            crypto.deriveKeys(PASSWORD, saltAuth, saltAuth.copyOf())
        }
    }

    @Test
    fun deriveKeys_rejectsEmptySalts() {
        assertThrows(IllegalArgumentException::class.java) {
            crypto.deriveKeys(PASSWORD, ByteArray(0), saltVault)
        }
        assertThrows(IllegalArgumentException::class.java) {
            crypto.deriveKeys(PASSWORD, saltAuth, ByteArray(0))
        }
    }

    @Test
    fun deriveKeys_rejectsIterationsBelowTheConfiguredMinimum() {
        // El servidor manda los parámetros: no puede degradar el KDF.
        val productionCrypto = CryptoManager()

        assertThrows(IllegalArgumentException::class.java) {
            productionCrypto.deriveKeys(
                masterPassword = PASSWORD,
                saltAuth = saltAuth,
                saltVault = saltVault,
                params = KdfParams(iterations = CryptoManager.MIN_ACCEPTED_ITERATIONS - 1),
            )
        }
    }

    @Test
    fun minAcceptedIterations_matchOwaspRecommendation() {
        assertTrue(
            "OWASP exige al menos 600 000 iteraciones para PBKDF2-HMAC-SHA256",
            CryptoManager.MIN_ACCEPTED_ITERATIONS >= 600_000,
        )
        assertEquals(CryptoManager.DEFAULT_ITERATIONS, CryptoManager.MIN_ACCEPTED_ITERATIONS)
    }

    @Test
    fun derivedKeys_toStringDoesNotExposeKeyMaterial() {
        val text = keys.toString()

        assertFalse(text.contains(keys.authHash.toHex()))
        assertFalse(text.contains(keys.vaultKey.encoded.toHex()))
    }

    // --- Vectores conocidos (RFC 7914 §11) -----------------------------------

    /**
     * Única prueba que verifica el algoritmo en sí: si alguien cambia
     * `PBKDF2WithHmacSHA256` por SHA-1 o ignora las iteraciones, las pruebas de
     * consistencia siguen en verde pero esta falla.
     *
     * PBKDF2 define `DK = T1 || T2 || ...`, así que los primeros 32 bytes de la
     * salida de 64 son exactamente la salida de 32: se compara con la mitad.
     */
    @Test
    fun deriveKeys_matchesRfc7914Vector1() {
        val result = crypto.deriveKeys(
            masterPassword = "passwd".toCharArray(),
            saltAuth = "salt".toByteArray(),
            saltVault = "vault".toByteArray(),
            params = KdfParams(iterations = 1),
        )

        assertEquals(
            "55ac046e56e3089fec1691c22544b605f94185216dde0465e68b9d57c20dacbc",
            result.authHash.toHex(),
        )
    }

    /**
     * Segundo vector del RFC 7914 §11, con 80 000 iteraciones: este sí detecta
     * que el parámetro de iteraciones se aplica de verdad (el de `c=1` no).
     */
    @Test
    fun deriveKeys_matchesRfc7914Vector2() {
        val result = crypto.deriveKeys(
            masterPassword = "Password".toCharArray(),
            saltAuth = "NaCl".toByteArray(),
            saltVault = "vault".toByteArray(),
            params = KdfParams(iterations = 80_000),
        )

        assertEquals(
            "4ddcd8f60b98be21830cee5ef22701f9641a4418d04c0414aeff08876b34ab56",
            result.authHash.toHex(),
        )
    }

    @Test
    fun deriveKeys_normalizesPasswordToNfc() {
        // "contraseña" precompuesta (NFC) contra "contrasen" + tilde combinante
        // + "a" (NFD). Sin normalizar dan hashes distintos y el usuario perdería
        // el acceso a su bóveda al cambiar de teclado.
        val nfc = "contrase\u00F1a"
        val nfd = "contrasen\u0303a"

        val fromNfc = crypto.deriveKeys(nfc.toCharArray(), "salt".toByteArray(), "vault".toByteArray(), KdfParams(iterations = 1000))
        val fromNfd = crypto.deriveKeys(nfd.toCharArray(), "salt".toByteArray(), "vault".toByteArray(), KdfParams(iterations = 1000))

        assertEquals(
            "f56f642ed03baf4162846ebc71daa3dd06966e88bb686d5f4c403640ca76d3ca",
            fromNfc.authHash.toHex(),
        )
        assertArrayEquals("NFD y NFC deben derivar la misma clave", fromNfc.authHash, fromNfd.authHash)
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
    fun decrypt_failsWhenAnyByteOfTheBlobIsAltered() {
        val aad = "entrada-1".toByteArray()
        val blob = crypto.encrypt("datos sensibles", key, aad = aad)

        blob.indices.forEach { index ->
            val tampered = blob.copyOf()
            tampered[index] = (tampered[index].toInt() xor 0x01).toByte()

            assertThrows(
                "alterar el byte $index debería fallar",
                CryptoException::class.java,
            ) {
                crypto.decrypt(tampered, key, aad = aad)
            }
        }
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
        val wrongKey = crypto.deriveKeys(
            masterPassword = PASSWORD,
            saltAuth = saltAuth,
            saltVault = "otro-salt-vault".toByteArray(),
            params = KdfParams(iterations = TEST_ITERATIONS),
        ).vaultKey
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

    private fun deriveTestKeys(iterations: Int = TEST_ITERATIONS) = crypto.deriveKeys(
        masterPassword = PASSWORD,
        saltAuth = saltAuth,
        saltVault = saltVault,
        params = KdfParams(iterations = iterations),
    )

    private companion object {
        private const val TEST_ITERATIONS = 1_000
        private val PASSWORD = "contrasenaMaestraSegura123".toCharArray()
    }
}

internal fun ByteArray.nonceOf(): ByteArray = copyOfRange(1, 1 + CryptoManager.NONCE_SIZE)

internal fun ByteArray.toHex(): String = joinToString(separator = "") { byte -> "%02x".format(byte) }
