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
 * Error de criptografía: cubre tanto la derivación de claves como el descifrado
 * fallido. Nunca incluye en el mensaje la contraseña, la clave ni el contenido.
 */
class CryptoException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Resultado de cifrar un bloque.
 *
 * - [ciphertext] ya incluye el tag de autenticación de GCM (16 bytes al final).
 * - [nonce] es el IV de 12 bytes. No es secreto, pero **debe** guardarse junto al
 *   ciphertext: sin él no se puede descifrar.
 *
 * `toString` no imprime los bytes para que nunca acaben en un log.
 */
class EncryptedData(
    val ciphertext: ByteArray,
    val nonce: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedData) return false
        return ciphertext.contentEquals(other.ciphertext) && nonce.contentEquals(other.nonce)
    }

    override fun hashCode(): Int = 31 * ciphertext.contentHashCode() + nonce.contentHashCode()

    override fun toString(): String =
        "EncryptedData(ciphertext=${ciphertext.size} bytes, nonce=${nonce.size} bytes)"
}

/**
 * Cifrado y derivación de claves de SecureUP.
 *
 * Decisiones (no cambiar sin revisarlas contra `AGENTS.md` §6-7):
 * - **PBKDF2WithHmacSHA256**, 600 000 iteraciones: es el valor que recomienda
 *   OWASP hoy para PBKDF2-HMAC-SHA256. `PBKDF2WithHmacSHA256` existe a partir de
 *   API 26, que es el `minSdk` del proyecto.
 * - **AES-256-GCM** con **nonce aleatorio de 12 bytes por operación**, generado
 *   con [SecureRandom]. 12 bytes es el tamaño recomendado para GCM y nunca se
 *   reutiliza: reutilizar un nonce con la misma clave rompe GCM por completo.
 * - No se usa ECB, ni claves fijas, ni `java.util.Random`.
 * - Esta clase **no persiste nada**: la clave vive en memoria y la decide quien
 *   la llama.
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
        require(size >= MIN_SALT_SIZE) { "Salt must be at least $MIN_SALT_SIZE bytes" }
        return ByteArray(size).also(secureRandom::nextBytes)
    }

    /**
     * Deriva una clave AES de 256 bits con PBKDF2-HMAC-SHA256.
     *
     * @param password contraseña en `CharArray` para que el llamador pueda
     *   borrarla con `fill('\u0000')` cuando termine la sesión.
     * @param salt salt del servidor o generado con [generateSalt]; distinto para
     *   `auth_hash` y para la clave de la bóveda.
     */
    fun deriveKey(
        password: CharArray,
        salt: ByteArray,
        iterations: Int = defaultIterations,
    ): SecretKey {
        require(salt.size >= MIN_SALT_SIZE) { "Salt must be at least $MIN_SALT_SIZE bytes" }
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
     * Cifra [plaintext] con [key]. Cada llamada usa un nonce nuevo.
     */
    fun encrypt(plaintext: ByteArray, key: SecretKey): EncryptedData {
        return try {
            val nonce = ByteArray(NONCE_SIZE).also(secureRandom::nextBytes)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_SIZE_BITS, nonce))
            val ciphertext = cipher.doFinal(plaintext)
            EncryptedData(ciphertext = ciphertext, nonce = nonce)
        } catch (e: GeneralSecurityException) {
            throw CryptoException("Encryption failed", e)
        }
    }

    /**
     * Descifra [data] con [key].
     *
     * GCM autentica: si el ciphertext o el nonce fueron alterados (aunque sea un
     * byte) el descifrado falla con [CryptoException]. Eso se trata siempre como
     * manipulación, nunca como un dato a medias.
     */
    fun decrypt(data: EncryptedData, key: SecretKey): ByteArray {
        if (data.nonce.size != NONCE_SIZE) {
            throw CryptoException("Invalid nonce length: expected $NONCE_SIZE bytes")
        }
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_SIZE_BITS, data.nonce))
            cipher.doFinal(data.ciphertext)
        } catch (e: AEADBadTagException) {
            // Autenticación fallida: ciphertext o nonce modificados, o clave incorrecta.
            throw CryptoException("Authentication failed", e)
        } catch (e: GeneralSecurityException) {
            throw CryptoException("Decryption failed", e)
        }
    }

    /** Cifra texto UTF-8. */
    fun encrypt(plaintext: String, key: SecretKey): EncryptedData =
        encrypt(plaintext.toByteArray(Charsets.UTF_8), key)

    /** Descifra y decodifica como texto UTF-8. */
    fun decryptAsString(data: EncryptedData, key: SecretKey): String =
        String(decrypt(data, key), Charsets.UTF_8)

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

        /** PBEKeySpec exige un salt de al menos 8 bytes; exigimos 16. */
        private const val MIN_SALT_SIZE = 16

        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val KEY_ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
