package com.secureup.app.data.crypto

import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.text.Normalizer
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.Mac
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
 * Parámetros del KDF tal como los publica el servidor (`/api/auth/salt`).
 *
 * Se reciben del exterior, así que son datos **no confiables**. [CryptoManager.deriveKeys]
 * los valida antes de derivar:
 *
 * - `version` debe ser [CURRENT_VERSION]; una versión desconocida se rechaza en
 *   vez de derivar con el algoritmo equivocado.
 * - `iterations` debe estar entre el mínimo configurado (600 000 en producción)
 *   y [CryptoManager.MAX_ACCEPTED_ITERATIONS]. Así un servidor comprometido no
 *   puede degradar el KDF ni colgar el dispositivo con un valor absurdo.
 *
 * @param version versión del algoritmo de derivación (1 = PBKDF2-HMAC-SHA256 +
 *   etiquetas HKDF).
 * @param iterations iteraciones de PBKDF2 con las que se derivó la `masterKey`.
 */
data class KdfParams(
    val version: Int = CURRENT_VERSION,
    val iterations: Int = CryptoManager.DEFAULT_ITERATIONS,
) {
    companion object {
        /** Única versión implementada hoy. */
        const val CURRENT_VERSION = 1
    }
}

/**
 * Resultado de derivar las dos claves de la sesión a partir de una sola
 * `masterKey`.
 *
 * - [authHash] va al servidor como prueba de autenticación; el backend lo vuelve
 *   a hashear (bcrypt/Argon2id) antes de guardarlo. **No** es la clave del vault:
 *   es `HMAC-SHA256(masterKey, "secureup-auth-v1" || 0x01)`.
 * - [vaultKey] **nunca sale del dispositivo**: cifra las entradas de la bóveda y
 *   es `HMAC-SHA256(masterKey, "secureup-vault-v1" || 0x01)`.
 *
 * El llamador debe borrar [authHash] con `fill(0)` en cuanto lo envíe. [vaultKey]
 * vive en memoria mientras dure la sesión y se descarta al cerrar sesión.
 */
class DerivedKeys(
    val authHash: ByteArray,
    val vaultKey: SecretKey,
) {
    /** No imprime material de clave: esto puede acabar en un log. */
    override fun toString(): String =
        "DerivedKeys(authHash=${authHash.size} bytes, vaultKey=<redacted>)"
}

/**
 * Cifrado y derivación de claves de SecureUP.
 *
 * ## Derivación
 *
 * Una sola derivación costosa y dos etiquetas:
 *
 * ```
 * masterKey = PBKDF2WithHmacSHA256(contraseña NFC, salt, iteraciones, 32 bytes)
 * authHash  = HMAC-SHA256(masterKey, "secureup-auth-v1"  || 0x01)
 * vaultKey  = HMAC-SHA256(masterKey, "secureup-vault-v1" || 0x01)
 * ```
 *
 * Las dos etiquetas son HKDF-Expand de un bloque (RFC 5869). **No** se usan dos
 * PBKDF2 con salts distintos: el servidor elige los salts y podría devolver el
 * `salt_vault` real como `salt_auth`, recibiendo entonces `vaultKey` disfrazada
 * de `auth_hash`. Con una sola derivación, `authHash` es una salida de HMAC con
 * otra etiqueta y el servidor no puede convertirla en la clave del vault.
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
 * - El AAD es **obligatorio** en la firma: quien cifra debe decidir
 *   explícitamente qué contexto ata al blob (en la bóveda, el UUID de la
 *   entrada). Un valor por defecto invitaría a olvidarlo.
 *
 * ## Decisiones (no cambiar sin revisar `AGENTS.md` §6-7)
 *
 * - **PBKDF2WithHmacSHA256**, 600 000 iteraciones: valor recomendado por OWASP
 *   para PBKDF2-HMAC-SHA256. `PBKDF2WithHmacSHA256` existe desde API 26.
 * - **AES-256-GCM** con nonce aleatorio de 12 bytes por operación, generado con
 *   [SecureRandom]. Reutilizar un nonce con la misma clave rompe GCM por completo.
 * - Nada de ECB, claves fijas ni `java.util.Random`.
 * - Esta clase **no persiste nada**.
 *
 * @param secureRandom inyectable solo para pruebas; en producción va el default.
 * @param minIterations mínimo aceptado de `KdfParams.iterations`. Los tests lo
 *   bajan para no pagar 600 000 iteraciones en cada caso.
 */
class CryptoManager(
    private val secureRandom: SecureRandom = SecureRandom(),
    private val minIterations: Int = MIN_ACCEPTED_ITERATIONS,
) {

    /**
     * Genera un salt aleatorio de 16 bytes. Cada usuario debe tener su propio
     * salt; se usa una sola vez por derivación.
     */
    fun generateSalt(size: Int = SALT_SIZE): ByteArray {
        require(size >= SALT_SIZE) { "Salt must be at least $SALT_SIZE bytes" }
        return ByteArray(size).also(secureRandom::nextBytes)
    }

    /**
     * Deriva `authHash` y `vaultKey` de la contraseña maestra.
     *
     * La contraseña se **normaliza a NFC antes de derivar**: "ñ" puede llegar
     * como un solo carácter (U+00F1) o como "n" + tilde combinante (U+0303).
     * Sin normalizar, la misma contraseña escrita en dos teclados distintos
     * produciría claves distintas y el usuario perdería el acceso a su bóveda.
     *
     * @param salt salt del servidor o generado con [generateSalt].
     * @throws IllegalArgumentException si el salt está vacío, si
     *   `params.version` no es [KdfParams.CURRENT_VERSION] o si
     *   `params.iterations` está fuera de `[minIterations, MAX_ACCEPTED_ITERATIONS]`.
     *
     * El llamador debe borrar `masterPassword` con `fill('\u0000')` en cuanto
     * termine la sesión: esta función no lo hace porque el `CharArray` es suyo.
     */
    fun deriveKeys(
        masterPassword: CharArray,
        salt: ByteArray,
        params: KdfParams = KdfParams(),
    ): DerivedKeys {
        require(salt.isNotEmpty()) { "Salt must not be empty" }
        require(params.version == KdfParams.CURRENT_VERSION) {
            "Unsupported KDF version: ${params.version}"
        }
        require(params.iterations >= minIterations) {
            "Refusing KDF iterations below the minimum ($minIterations)"
        }
        require(params.iterations <= MAX_ACCEPTED_ITERATIONS) {
            "Refusing KDF iterations above the maximum ($MAX_ACCEPTED_ITERATIONS)"
        }

        val masterKey = deriveMasterKey(masterPassword, salt, params.iterations)
        try {
            val authHash = expand(masterKey, AUTH_INFO)
            val vaultKeyBytes = expand(masterKey, VAULT_INFO)
            try {
                // SecretKeySpec copia los bytes: podemos borrar el original.
                return DerivedKeys(authHash, SecretKeySpec(vaultKeyBytes, KEY_ALGORITHM))
            } finally {
                vaultKeyBytes.fill(0)
            }
        } finally {
            masterKey.fill(0)
        }
    }

    /**
     * Deriva la `masterKey` de 32 bytes con PBKDF2-HMAC-SHA256.
     *
     * `internal` para poder contrastar los vectores del RFC 7914 §11 por
     * separado. La contraseña se normaliza a NFC aquí dentro.
     *
     * El `ByteArray` devuelto es del llamador: debe borrarlo con `fill(0)`.
     * La contraseña de entrada **no** se borra (la posee el llamador); se borra
     * la copia normalizada que crea esta función.
     *
     * Limitación conocida: la normalización obliga a crear un `String`
     * inmutable que no se puede borrar de memoria.
     */
    internal fun deriveMasterKey(
        masterPassword: CharArray,
        salt: ByteArray,
        iterations: Int,
    ): ByteArray {
        require(salt.isNotEmpty()) { "Salt must not be empty" }
        require(iterations >= 1) { "Iterations must be positive" }

        val password = Normalizer.normalize(String(masterPassword), Normalizer.Form.NFC).toCharArray()
        val spec = PBEKeySpec(password, salt, iterations, KEY_SIZE_BITS)
        try {
            return SecretKeyFactory.getInstance(PBKDF2_ALGORITHM).generateSecret(spec).encoded
        } catch (e: GeneralSecurityException) {
            throw CryptoException("Key derivation failed", e)
        } finally {
            // Borra la copia interna que PBEKeySpec guarda de la contraseña.
            spec.clearPassword()
            password.fill('\u0000')
        }
    }

    /**
     * HKDF-Expand de **un solo bloque** (RFC 5869): `HMAC-SHA256(prk, info || 0x01)`.
     *
     * Alcanza para 32 bytes, que es todo lo que necesitamos (la salida de
     * HMAC-SHA256). No se implementa el bucle de bloques porque no hace falta y
     * más código criptográfico es más superficie de error.
     *
     * `internal` para poder contrastarlo con el caso 1 del RFC 5869.
     */
    internal fun expand(prk: ByteArray, info: ByteArray): ByteArray {
        require(prk.isNotEmpty()) { "PRK must not be empty" }

        return try {
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            mac.init(SecretKeySpec(prk, HMAC_ALGORITHM))
            mac.update(info)
            mac.update(HKDF_BLOCK_INDEX)
            mac.doFinal()
        } catch (e: GeneralSecurityException) {
            throw CryptoException("Key expansion failed", e)
        }
    }

    /**
     * Cifra [plaintext] y devuelve el blob `[versión][nonce][ciphertext+tag]`.
     *
     * El llamador debe borrar [plaintext] con `fill(0)` cuando ya no lo necesite.
     *
     * @param aad datos asociados que no se cifran pero quedan autenticados. Para
     *   una entrada de la bóveda se pasa su UUID; si luego se descifra con otro
     *   AAD, falla. Es obligatorio a propósito: olvidarlo dejaría el blob sin
     *   atar a su contexto.
     */
    fun encrypt(
        plaintext: ByteArray,
        vaultKey: SecretKey,
        aad: ByteArray,
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
            // `sealed` es copia nuestra: se borra para no dejarla en el heap.
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
     *
     * El `ByteArray` devuelto es del llamador: debe borrarlo con `fill(0)` en
     * cuanto deje de usarlo.
     */
    fun decrypt(
        blob: ByteArray,
        vaultKey: SecretKey,
        aad: ByteArray,
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
        aad: ByteArray,
    ): ByteArray = encrypt(plaintext.toByteArray(Charsets.UTF_8), vaultKey, aad)

    /**
     * Descifra y decodifica como texto UTF-8.
     *
     * Limitación conocida: el `String` resultante es inmutable y no se puede
     * borrar; para secretos que se puedan quedar en memoria, usar [decrypt].
     */
    fun decryptAsString(
        blob: ByteArray,
        vaultKey: SecretKey,
        aad: ByteArray,
    ): String = String(decrypt(blob, vaultKey, aad), Charsets.UTF_8)

    companion object {
        /** Iteraciones recomendadas por OWASP para PBKDF2-HMAC-SHA256. */
        const val DEFAULT_ITERATIONS = 600_000

        /** Mínimo que el cliente acepta del servidor. Igual al recomendado. */
        const val MIN_ACCEPTED_ITERATIONS = 600_000

        /**
         * Máximo que el cliente acepta del servidor: 5 veces el valor por
         * defecto. Evita que un servidor comprometido pida un número absurdo de
         * iteraciones y bloquee el dispositivo.
         */
        const val MAX_ACCEPTED_ITERATIONS = 5 * DEFAULT_ITERATIONS

        /** Tamaño de clave AES-256 (también de `masterKey` y `authHash`). */
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

        /** Etiquetas de dominio: separan authHash de vaultKey (HKDF-Expand). */
        private val AUTH_INFO = "secureup-auth-v1".toByteArray(Charsets.UTF_8)
        private val VAULT_INFO = "secureup-vault-v1".toByteArray(Charsets.UTF_8)

        /** Contador de bloque de HKDF-Expand: siempre 0x01 con un solo bloque. */
        private val HKDF_BLOCK_INDEX = byteArrayOf(0x01)

        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val HMAC_ALGORITHM = "HmacSHA256"
        private const val KEY_ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
