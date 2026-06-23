package com.time.applauncher.goalgaurd.core.crypto

import kotlinx.serialization.Serializable
import java.security.GeneralSecurityException
import java.security.SecureRandom
import kotlin.io.encoding.Base64

/**
 * The portable, passphrase-protected wrapper around the data-encryption key (DEK). Stored locally
 * and (optionally) on the server for multi-device unlock — it is useless without the passphrase,
 * so the server stays zero-knowledge.
 *
 * The passphrase derives a key-encryption key (KEK) via [Pbkdf2]; the KEK AES-GCM-wraps the random
 * DEK. Changing the passphrase only re-wraps the DEK (see [VaultCrypto.rewrap]) — data is never
 * re-encrypted.
 */
@Serializable
data class KeyEnvelope(
    val salt: String,        // base64 PBKDF2 salt
    val iterations: Int,     // PBKDF2 iteration count
    val wrappedDek: String,  // EncryptedBlob.encode() of the DEK under the KEK
    val version: Int = 1,
)

object VaultCrypto {

    /** OWASP-recommended floor for PBKDF2-HMAC-SHA256. */
    const val DEFAULT_ITERATIONS = 210_000
    private const val DEK_BYTES = 32
    private const val SALT_BYTES = 16

    private val aead = AesGcmAead()
    private val random = SecureRandom()

    fun newDek(): ByteArray = ByteArray(DEK_BYTES).also { random.nextBytes(it) }

    /**
     * Creates a fresh envelope for [passphrase], wrapping [dek] (a new random DEK by default).
     * Returns the envelope to persist plus the raw DEK to load into the [VaultKeyManager].
     */
    fun createEnvelope(
        passphrase: String,
        dek: ByteArray = newDek(),
        iterations: Int = DEFAULT_ITERATIONS,
    ): Pair<KeyEnvelope, ByteArray> {
        val salt = ByteArray(SALT_BYTES).also { random.nextBytes(it) }
        val kek = Pbkdf2.deriveKey(passphrase, salt, iterations)
        val wrapped = aead.encrypt(dek, kek)
        return KeyEnvelope(Base64.Default.encode(salt), iterations, wrapped.encode()) to dek
    }

    /** Recovers the DEK from [envelope] using [passphrase]. Throws [WrongPassphraseException] on mismatch. */
    fun unwrap(envelope: KeyEnvelope, passphrase: String): ByteArray {
        val salt = Base64.Default.decode(envelope.salt)
        val kek = Pbkdf2.deriveKey(passphrase, salt, envelope.iterations)
        return try {
            aead.decrypt(EncryptedBlob.decode(envelope.wrappedDek), kek)
        } catch (_: GeneralSecurityException) {
            throw WrongPassphraseException()
        }
    }

    /** Re-wraps the same DEK under a new passphrase. Verifies [oldPassphrase] first. */
    fun rewrap(envelope: KeyEnvelope, oldPassphrase: String, newPassphrase: String): KeyEnvelope {
        val dek = unwrap(envelope, oldPassphrase)
        return createEnvelope(newPassphrase, dek).first
    }
}
