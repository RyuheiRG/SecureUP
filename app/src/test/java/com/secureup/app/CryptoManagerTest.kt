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
 * [kdfLimits_matchOwaspRecommendation].
 */
class CryptoManagerTest {

    private val crypto = CryptoManager(minIterations = 1)
    private val salt = "salt".toByteArray()
    private val keys = crypto.deriveKeys(
        masterPassword = PASSWORD,
        salt = salt,
        params = KdfParams(iterations = TEST_ITERATIONS),
    )
    private val key = keys.vaultKey

    // --- Derivación de claves -------------------------------------------------

    @Test
    fun deriveKeys_isDeterministicForSamePasswordAndSalt() {
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
    fun deriveKeys_authHashDiffersFromVaultKeyAndMasterKey() {
        val masterKey = crypto.deriveMasterKey(PASSWORD, salt, TEST_ITERATIONS)
        try {
            assertFalse("authHash no debe ser la vaultKey", keys.authHash.contentEquals(keys.vaultKey.encoded))
            assertFalse("authHash no debe ser la masterKey", keys.authHash.contentEquals(masterKey))
            assertFalse("vaultKey no debe ser la masterKey", keys.vaultKey.encoded.contentEquals(masterKey))
        } finally {
            masterKey.fill(0)
        }
    }

    @Test
    fun deriveKeys_changingSaltChangesBothKeys() {
        val otherKeys = crypto.deriveKeys(
            masterPassword = PASSWORD,
            salt = "otro-salt".toByteArray(),
            params = KdfParams(iterations = TEST_ITERATIONS),
        )

        assertFalse(keys.authHash.contentEquals(otherKeys.authHash))
        assertFalse(keys.vaultKey.encoded.contentEquals(otherKeys.vaultKey.encoded))
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
        val result = crypto.deriveKeys(PASSWORD, "salt".toByteArray(), KdfParams(iterations = 1))

        assertEquals(32, result.authHash.size)
    }

    @Test
    fun deriveKeys_rejectsEmptySalt() {
        assertThrows(IllegalArgumentException::class.java) {
            crypto.deriveKeys(PASSWORD, ByteArray(0), KdfParams(iterations = TEST_ITERATIONS))
        }
    }

    @Test
    fun deriveKeys_rejectsUnknownVersion() {
        listOf(0, 2, Int.MAX_VALUE).forEach { version ->
            assertThrows(
                "la versión $version debe rechazarse",
                IllegalArgumentException::class.java,
            ) {
                crypto.deriveKeys(
                    masterPassword = PASSWORD,
                    salt = salt,
                    params = KdfParams(version = version, iterations = TEST_ITERATIONS),
                )
            }
        }
    }

    @Test
    fun deriveKeys_rejectsIterationsBelowTheConfiguredMinimum() {
        // El servidor manda los parámetros: no puede degradar el KDF.
        val productionCrypto = CryptoManager()

        assertThrows(IllegalArgumentException::class.java) {
            productionCrypto.deriveKeys(
                masterPassword = PASSWORD,
                salt = salt,
                params = KdfParams(iterations = CryptoManager.MIN_ACCEPTED_ITERATIONS - 1),
            )
        }
    }

    @Test
    fun deriveKeys_rejectsIterationsAboveTheMaximum() {
        // Ni degradar ni colgar el dispositivo con un número absurdo.
        val productionCrypto = CryptoManager()

        assertThrows(IllegalArgumentException::class.java) {
            productionCrypto.deriveKeys(
                masterPassword = PASSWORD,
                salt = salt,
                params = KdfParams(iterations = CryptoManager.MAX_ACCEPTED_ITERATIONS + 1),
            )
        }
    }

    @Test
    fun kdfLimits_matchOwaspRecommendation() {
        assertTrue(
            "OWASP exige al menos 600 000 iteraciones para PBKDF2-HMAC-SHA256",
            CryptoManager.MIN_ACCEPTED_ITERATIONS >= 600_000,
        )
        assertEquals(CryptoManager.DEFAULT_ITERATIONS, CryptoManager.MIN_ACCEPTED_ITERATIONS)
        assertEquals(5 * CryptoManager.DEFAULT_ITERATIONS, CryptoManager.MAX_ACCEPTED_ITERATIONS)
        assertTrue(CryptoManager.MAX_ACCEPTED_ITERATIONS > CryptoManager.MIN_ACCEPTED_ITERATIONS)
    }

    @Test
    fun derivedKeys_toStringDoesNotExposeKeyMaterial() {
        val text = keys.toString()

        assertFalse(text.contains(keys.authHash.toHex()))
        assertFalse(text.contains(keys.vaultKey.encoded.toHex()))
    }

    // --- Vectores conocidos ---------------------------------------------------

    /**
     * Vector 1 del RFC 7914 §11. Única prueba que verifica el algoritmo en sí:
     * si alguien cambia `PBKDF2WithHmacSHA256` por SHA-1 o ignora las
     * iteraciones, las pruebas de consistencia siguen en verde pero esta falla.
     *
     * PBKDF2 define `DK = T1 || T2 || ...`, así que los primeros 32 bytes de la
     * salida de 64 son exactamente la salida de 32: se compara con la mitad.
     */
    @Test
    fun deriveMasterKey_matchesRfc7914Vector1() {
        val masterKey = crypto.deriveMasterKey("passwd".toCharArray(), "salt".toByteArray(), iterations = 1)
        try {
            assertEquals(
                "55ac046e56e3089fec1691c22544b605f94185216dde0465e68b9d57c20dacbc",
                masterKey.toHex(),
            )
        } finally {
            masterKey.fill(0)
        }
    }

    /**
     * Vector 2 del RFC 7914 §11, con 80 000 iteraciones: este sí detecta que el
     * parámetro de iteraciones se aplica de verdad (el de `c=1` no).
     */
    @Test
    fun deriveMasterKey_matchesRfc7914Vector2() {
        val masterKey = crypto.deriveMasterKey("Password".toCharArray(), "NaCl".toByteArray(), iterations = 80_000)
        try {
            assertEquals(
                "4ddcd8f60b98be21830cee5ef22701f9641a4418d04c0414aeff08876b34ab56",
                masterKey.toHex(),
            )
        } finally {
            masterKey.fill(0)
        }
    }

    @Test
    fun deriveMasterKey_normalizesPasswordToNfc() {
        // "contraseña" precompuesta (NFC) contra "contrasen" + tilde combinante
        // + "a" (NFD). Sin normalizar dan hashes distintos y el usuario perdería
        // el acceso a su bóveda al cambiar de teclado.
        val nfc = "contrase\u00F1a"
        val nfd = "contrasen\u0303a"

        val fromNfc = crypto.deriveMasterKey(nfc.toCharArray(), "salt".toByteArray(), iterations = 1000)
        val fromNfd = crypto.deriveMasterKey(nfd.toCharArray(), "salt".toByteArray(), iterations = 1000)
        try {
            assertEquals(
                "f56f642ed03baf4162846ebc71daa3dd06966e88bb686d5f4c403640ca76d3ca",
                fromNfc.toHex(),
            )
            assertArrayEquals("NFD y NFC deben derivar la misma masterKey", fromNfc, fromNfd)
        } finally {
            fromNfc.fill(0)
            fromNfd.fill(0)
        }
    }

    /**
     * Caso 1 del RFC 5869: `HMAC-SHA256(PRK, info || 0x01)`. Comprueba que la
     * expansión por etiquetas es un HKDF-Expand de verdad y no un HMAC casero.
     */
    @Test
    fun expand_matchesRfc5869TestCase1() {
        val prk = "077709362c2e32df0ddc3f0dc47bba6390b6c73bb50f9c3122ec844ad7c2b3e5".hexToBytes()
        val info = "f0f1f2f3f4f5f6f7f8f9".hexToBytes()

        val okm = crypto.expand(prk, info)
        try {
            assertEquals(32, okm.size)
            assertEquals(
                "3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf",
                okm.toHex(),
            )
        } finally {
            prk.fill(0)
            okm.fill(0)
        }
    }

    /**
     * Esquema completo con el vector 1 del RFC 7914. Este valor **no** viene de
     * un RFC: es la definición de SecureUP, así que fija la construcción.
     */
    @Test
    fun deriveKeys_matchesTheLabeledScheme() {
        val result = crypto.deriveKeys(
            masterPassword = "passwd".toCharArray(),
            salt = "salt".toByteArray(),
            params = KdfParams(iterations = 1),
        )

        assertEquals(
            "dbe363ce336a712bdcfb3272fa6995d7166535091cd5630dde8f556c6d503d16",
            result.authHash.toHex(),
        )
        assertEquals(
            "7bb3d0dc1206b66345c3b010f2aae3a0cbdcc027ab01348dec12ae2270846737",
            result.vaultKey.encoded.toHex(),
        )
    }

    /**
     * Regresión del ataque que motivó el rediseño: con dos PBKDF2 y salts del
     * servidor, `authHash` era `PBKDF2(pw, saltAuth)` y la clave del vault
     * `PBKDF2(pw, saltVault)`, así que un servidor malicioso podía devolver
     * `saltVault` como `saltAuth` y recibir la clave del vault como `authHash`.
     *
     * Con una sola derivación y etiquetas distintas, ningún `authHash` de
     * ningún salt puede coincidir con la `vaultKey` de ningún salt.
     */
    @Test
    fun authHash_cannotBeExchangedForAVaultKey() {
        val derived = listOf("salt", "otro-salt", "salt-vault").map { candidate ->
            crypto.deriveKeys(PASSWORD, candidate.toByteArray(), KdfParams(iterations = TEST_ITERATIONS))
        }

        derived.forEachIndexed { authIndex, authKeys ->
            derived.forEachIndexed { vaultIndex, vaultKeys ->
                assertFalse(
                    "authHash con salt $authIndex no debe coincidir con la vaultKey del salt $vaultIndex",
                    authKeys.authHash.contentEquals(vaultKeys.vaultKey.encoded),
                )
            }
        }
    }

    // --- Cifrado / descifrado -------------------------------------------------

    @Test
    fun encryptThenDecrypt_returnsOriginalText() {
        val original = "contraseña con acentos y símbolos: ñ@€"

        val blob = crypto.encrypt(original, key, byteArrayOf())

        assertEquals(original, crypto.decryptAsString(blob, key, byteArrayOf()))
    }

    @Test
    fun encryptThenDecrypt_handlesEmptyAndBinaryPayloads() {
        val empty = crypto.encrypt(ByteArray(0), key, byteArrayOf())
        assertArrayEquals(ByteArray(0), crypto.decrypt(empty, key, byteArrayOf()))

        val binary = ByteArray(256) { it.toByte() }
        val blob = crypto.encrypt(binary, key, byteArrayOf())
        assertArrayEquals(binary, crypto.decrypt(blob, key, byteArrayOf()))
    }

    @Test
    fun blob_hasVersionByteAndNonce() {
        val blob = crypto.encrypt("datos", key, byteArrayOf())

        assertEquals(CryptoManager.BLOB_VERSION, blob[0].toInt())
        assertEquals(CryptoManager.MIN_BLOB_SIZE + "datos".length, blob.size)
    }

    @Test
    fun encrypt_usesAFreshNoncePerOperation() {
        val plaintext = "misma contraseña, misma key".toByteArray()
        val nonces = mutableSetOf<String>()
        val blobs = mutableSetOf<String>()

        repeat(100) {
            val blob = crypto.encrypt(plaintext, key, byteArrayOf())
            nonces += blob.nonceOf().toHex()
            blobs += blob.toHex()
            // Cada resultado debe seguir siendo descifrable con su propio nonce.
            assertArrayEquals(plaintext, crypto.decrypt(blob, key, byteArrayOf()))
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
        val blob = crypto.encrypt("datos sensibles", key, byteArrayOf())
        blob[0] = 0x02

        assertThrows(CryptoException::class.java) { crypto.decrypt(blob, key, byteArrayOf()) }
    }

    @Test
    fun decrypt_withTruncatedBlob_throws() {
        val blob = crypto.encrypt("datos sensibles", key, byteArrayOf())

        assertThrows(CryptoException::class.java) {
            crypto.decrypt(blob.copyOf(CryptoManager.MIN_BLOB_SIZE - 1), key, byteArrayOf())
        }
        assertThrows(CryptoException::class.java) {
            crypto.decrypt(ByteArray(0), key, byteArrayOf())
        }
    }

    @Test
    fun decrypt_withWrongKey_throws() {
        val wrongKey = crypto.deriveKeys(
            masterPassword = PASSWORD,
            salt = "otro-salt".toByteArray(),
            params = KdfParams(iterations = TEST_ITERATIONS),
        ).vaultKey
        val blob = crypto.encrypt("datos sensibles", key, byteArrayOf())

        assertThrows(CryptoException::class.java) { crypto.decrypt(blob, wrongKey, byteArrayOf()) }
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
            crypto.decrypt(blob, key, aad = byteArrayOf())
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
        salt = salt,
        params = KdfParams(iterations = iterations),
    )

    private companion object {
        private const val TEST_ITERATIONS = 1_000
        private val PASSWORD = "contrasenaMaestraSegura123".toCharArray()
    }
}

internal fun ByteArray.nonceOf(): ByteArray = copyOfRange(1, 1 + CryptoManager.NONCE_SIZE)

internal fun ByteArray.toHex(): String = joinToString(separator = "") { byte -> "%02x".format(byte) }

internal fun String.hexToBytes(): ByteArray =
    chunked(2).map { pair -> pair.toInt(16).toByte() }.toByteArray()
