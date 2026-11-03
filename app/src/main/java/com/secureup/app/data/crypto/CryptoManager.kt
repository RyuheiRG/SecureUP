package com.secureup.app.data.crypto

import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Error de criptografía: cubre tanto la derivación de claves como el cifrado y
 * el descifrado fallidos. Nunca incluye en el mensaje la contraseña, la clave ni
 * el contenido cifrado.
 */
class CryptoException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Cifrado y derivación de claves de SecureUP.
 *
 * ## Blob cifrado
 *
 * `encrypt` devuelve un único `ByteArray` autocontenido:
 *
 * ```
 * [0x01][nonce 12 bytes][ciphertext + tag GCM de 16 bytes]
 * ```
 *
 * - El primer byte es la **versión del formato**, para poder cambiar de
 *   algoritmo sabiendo qué formato tiene cada fila guardada.
 * - El nonce **no es secreto**, pero debe guardarse: sin él no se descifra.
 *
 * ## Decisiones (no cambiar sin revisar `AGENTS.md` §6-7)
 *
 * - **PBKDF2WithHmacSHA256**, 600 000 iteraciones: valor recomendado por OWASP
 *   para PBKDF2-HMAC-SHA256. `PBKDF2WithHmacSHA256` existe desde API 26.
 * - **AES-256-GCM** con nonce aleatorio de 12 bytes por operación, generado con
 *   [SecureRandom]. Reutilizar un nonce con la misma clave rompe GCM por completo.
 * - **AAD**: se cifra con datos asociados para atar el blob a su contexto. En la
 *   bóveda es el UUID de la entrada, y así el servidor no puede intercambiar
 *   blobs entre entradas.
 * - Nada de ECB, claves fijas ni `java.util.Random`.
 * - Esta clase **no persiste nada**.
 *
 * @param secureRandom inyectable solo para pruebas; en producción va el default.
 * @param defaultIterations iteraciones de PBKDF2 cuando no se pasan explícitas.
 */
class CryptoManager(
    private val secureRandom: SecureRandom = SecureRandom(),
    private val defaultIterations: Int = DEFAULT_ITERATIONS,
) {

    /**
     * Genera un salt aleatorio para PBKDF2. Cada derivación (autenticación y
     * bóveda) debe usar su **propio** salt.
     */
    fun generateSalt(size: Int = SALT_SIZE): ByteArray {
        require(size >= SALT_SIZE) { "Salt must be at least $SALT_SIZE bytes" }
        return ByteArray(size).also(secureRandom::nextBytes)
    }

    /**
     * Deriva una clave AES de 256 bits con PBKDF2-HMAC-SHA256.
     *
     * @param password contraseña en `CharArray` para que el llamador pueda
     *   borrarla con `fill('\u0000')` cuando termine la sesión.
     * @param salt salt del servidor o generado con [generateSalt].
     */
    fun deriveKey(
        password: CharArray,
        salt: ByteArray,
        iterations: Int = defaultIterations,
    ): SecretKey {
        require(salt.isNotEmpty()) { "Salt must not be empty" }
        require(iterations >= 1) { "Iterations must be positive" }

        val spec = PBEKeySpec(password, salt, iterations, KEY_SIZE_BITS)
        try {
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val keyBytes = factory.generateSecret(spec).encoded
            try {
                // SecretKeySpec copia el arreglo, así que podemos borrar el original.
                return SecretKeySpec(keyBytes, KEY_ALGORITHM)
            } finally {
                keyBytes.fill(0)
            }
        } catch (e: GeneralSecurityException) {
            throw CryptoException("Key derivation failed", e)
        } finally {
            // Borra la copia interna que PBEKeySpec guarda de la contraseña.
            spec.clearPassword()
        }
    }

    /**
     * Cifra [plaintext] y devuelve el blob `[versión][nonce][ciphertext+tag]`.
     *
     * @param aad datos asociados que no se cifran pero quedan autenticados. Para
     *   una entrada de la bóveda se pasa su UUID; si luego se descifra con otro
     *   AAD, falla.
     */
    fun encrypt(
        plaintext: ByteArray,
        vaultKey: SecretKey,
        aad: ByteArray = ByteArray(0),
    ): ByteArray {
        return try {
            val nonce = ByteArray(NONCE_SIZE).also(secureRandom::nextBytes)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, vaultKey, GCMParameterSpec(TAG_SIZE_BITS, nonce))
            if (aad.isNotEmpty()) {
                cipher.updateAAD(aad)
            }
            val sealed = cipher.doFinal(plaintext)

            val blob = ByteArray(HEADER_SIZE + sealed.size)
            blob[0] = BLOB_VERSION.toByte()
            nonce.copyInto(blob, destinationOffset = 1)
            sealed.copyInto(blob, destinationOffset = HEADER_SIZE)
            // El arreglo `sealed` es copia nuestra: se borra para no dejarlo en el heap.
            sealed.fill(0)
            blob
        } catch (e: GeneralSecurityException) {
            throw CryptoException("Encryption failed", e)
        }
    }

    /**
     * Descifra un blob creado por [encrypt].
     *
     * Valida versión y longitud antes de tocar el cifrado. Cualquier fallo
     * (blob truncado, versión desconocida, byte alterado, AAD o clave
     * incorrectos) lanza [CryptoException] con un mensaje genérico; el detalle
     * queda en `cause` para depurar. Nunca devuelve datos a medias.
     */
    fun decrypt(
        blob: ByteArray,
        vaultKey: SecretKey,
        aad: ByteArray = ByteArray(0),
    ): ByteArray {
        if (blob.size < MIN_BLOB_SIZE) {
            throw CryptoException(DECRYPTION_FAILED, IllegalArgumentException("Blob too short"))
        }
        if (blob[0].toInt() != BLOB_VERSION) {
            throw CryptoException(DECRYPTION_FAILED, IllegalArgumentException("Unknown blob version"))
        }

        val nonce = blob.copyOfRange(1, HEADER_SIZE)
        val sealed = blob.copyOfRange(HEADER_SIZE, blob.size)
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, vaultKey, GCMParameterSpec(TAG_SIZE_BITS, nonce))
            if (aad.isNotEmpty()) {
                cipher.updateAAD(aad)
            }
            cipher.doFinal(sealed)
        } catch (e: AEADBadTagException) {
            // Autenticación fallida: blobs intercambiados, byte alterado o clave incorrecta.
            throw CryptoException(DECRYPTION_FAILED, e)
        } catch (e: GeneralSecurityException) {
            throw CryptoException(DECRYPTION_FAILED, e)
        } finally {
            nonce.fill(0)
            sealed.fill(0)
        }
    }

    /** Cifra texto UTF-8. */
    fun encrypt(
        plaintext: String,
        vaultKey: SecretKey,
        aad: ByteArray = ByteArray(0),
    ): ByteArray = encrypt(plaintext.toByteArray(Charsets.UTF_8), vaultKey, aad)

    /** Descifra y decodifica como texto UTF-8. */
    fun decryptAsString(
        blob: ByteArray,
        vaultKey: SecretKey,
        aad: ByteArray = ByteArray(0),
    ): String = String(decrypt(blob, vaultKey, aad), Charsets.UTF_8)

    companion object {
        /** Iteraciones recomendadas por OWASP para PBKDF2-HMAC-SHA256. */
        const val DEFAULT_ITERATIONS = 600_000

        /** Tamaño de clave AES-256. */
        const val KEY_SIZE_BITS = 256

        /** Tamaño del tag de autenticación de GCM. */
        const val TAG_SIZE_BITS = 128

        /** IV de GCM: 12 bytes (96 bits) es el tamaño recomendado. */
        const val NONCE_SIZE = 12

        /** Salt de PBKDF2. */
        const val SALT_SIZE = 16

        /** Versión del formato del blob. */
        const val BLOB_VERSION = 1

        /** 1 byte de versión + nonce. */
        private const val HEADER_SIZE = 1 + NONCE_SIZE

        /** Blob mínimo válido: cabecera + tag, con plaintext vacío. */
        const val MIN_BLOB_SIZE = HEADER_SIZE + TAG_SIZE_BITS / 8

        /** Mensaje único de fallo de descifrado: no revela la causa real. */
        private const val DECRYPTION_FAILED = "Decryption failed"

        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val KEY_ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
