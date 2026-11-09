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
    fun expand_matchesRfc5869TestCase1() {
        val prk = "077709362c2e32df0ddc3f0dc47bba6390b6c73bb50f9c3122ec844ad7c2b3e5".hexToBytes()
        val info = "f0f1f2f3f4f5f6f7f8f9".hexToBytes()

        val okm = crypto.expand(prk, info)
        try {
            assertEquals(
                "3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf",
                okm.toHex(),
            )
        } finally {
            prk.fill(0)
            okm.fill(0)
        }
    }

    @Test
    fun deriveMasterKey_normalizesPasswordToNfc() {
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
     * GCM con AAD en Conscrypt: ida y vuelta y rechazo cuando el AAD no coincide.
     */
    @Test
    fun encryptAndDecrypt_withAad_roundTripsOnDevice() {
        val keys = crypto.deriveKeys(
            masterPassword = "contrasenaMaestraSegura123".toCharArray(),
            salt = crypto.generateSalt(),
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

private fun String.hexToBytes(): ByteArray =
    chunked(2).map { pair -> pair.toInt(16).toByte() }.toByteArray()
