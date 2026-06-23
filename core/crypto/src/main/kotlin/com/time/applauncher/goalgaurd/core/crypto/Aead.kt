package com.time.applauncher.goalgaurd.core.crypto

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/** Authenticated symmetric encryption. Decryption fails (throws) on a wrong key or tampered input. */
interface Aead {
    fun encrypt(plaintext: ByteArray, key: ByteArray, aad: ByteArray? = null): EncryptedBlob
    fun decrypt(blob: EncryptedBlob, key: ByteArray, aad: ByteArray? = null): ByteArray
}

/**
 * AES-256-GCM via the platform JCE. A fresh 96-bit random nonce is generated per [encrypt]; the
 * 128-bit tag is appended to the ciphertext. `key` must be 32 bytes. Available on Android API 24+.
 */
class AesGcmAead : Aead {

    private val random = SecureRandom()

    override fun encrypt(plaintext: ByteArray, key: ByteArray, aad: ByteArray?): EncryptedBlob {
        require(key.size == KEY_BYTES) { "AES-256 key must be $KEY_BYTES bytes, was ${key.size}" }
        val nonce = ByteArray(NONCE_BYTES).also { random.nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, nonce))
            aad?.let { updateAAD(it) }
        }
        return EncryptedBlob(nonce, cipher.doFinal(plaintext))
    }

    override fun decrypt(blob: EncryptedBlob, key: ByteArray, aad: ByteArray?): ByteArray {
        require(key.size == KEY_BYTES) { "AES-256 key must be $KEY_BYTES bytes, was ${key.size}" }
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, blob.nonce))
            aad?.let { updateAAD(it) }
        }
        return cipher.doFinal(blob.ciphertext) // AEADBadTagException on wrong key / tamper
    }

    private companion object {
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_BYTES = 32
        const val NONCE_BYTES = 12
        const val TAG_BITS = 128
    }
}
