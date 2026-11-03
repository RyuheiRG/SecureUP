package com.secureup.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.secureup.app.data.crypto.CryptoException
import com.secureup.app.data.crypto.CryptoManager
import com.secureup.app.data.crypto.KdfParams
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Los mismos vectores conocidos que `CryptoManagerTest`, pero ejecutados en un
 * dispositivo o emulador: valida que el proveedor criptográfico de Android
 * (Conscrypt) da exactamente los mismos resultados que el JVM (SunJCE).
 *
 * `testDebugUnitTest` NO ejecuta este archivo. Para lanzarlo hace falta un
 * dispositivo conectado (`adb devices`) y:
 *
 * ```
 * .\gradlew connectedDebugAndroidTest
 * ```
 */
@RunWith(AndroidJUnit4::class)
class CryptoManagerDeviceTest {

    private val crypto = CryptoManager(minIterations = 1)

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

    /**
     * GCM con AAD en Conscrypt: ida y vuelta y rechazo cuando el AAD no coincide.
     */
    @Test
    fun encryptAndDecrypt_withAad_roundTripsOnDevice() {
        val keys = crypto.deriveKeys(
            masterPassword = "contrasenaMaestraSegura123".toCharArray(),
            saltAuth = crypto.generateSalt(),
            saltVault = crypto.generateSalt(),
            params = KdfParams(iterations = 1000),
        )
        val entryId = "9f1c0a2e-4b7d-4c1a-9c3e-2d5b6a7c8e9f".toByteArray()

        val blob = crypto.encrypt("secreto de la bóveda", keys.vaultKey, aad = entryId)

        assertEquals(
            "secreto de la bóveda",
            crypto.decryptAsString(blob, keys.vaultKey, aad = entryId),
        )
        assertThrows(CryptoException::class.java) {
            crypto.decrypt(blob, keys.vaultKey, aad = "otro-id".toByteArray())
        }
    }
}

private fun ByteArray.toHex(): String = joinToString(separator = "") { byte -> "%02x".format(byte) }
