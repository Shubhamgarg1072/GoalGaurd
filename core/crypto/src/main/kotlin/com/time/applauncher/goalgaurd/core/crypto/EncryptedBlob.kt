package com.time.applauncher.goalgaurd.core.crypto

import kotlin.io.encoding.Base64

/**
 * An AES-GCM ciphertext together with the random nonce it was produced with. The 128-bit GCM
 * authentication tag is appended to [ciphertext] by the JCE, so this pair is self-authenticating.
 *
 * [encode] serialises to a compact `base64(nonce).base64(ciphertext)` string for storage/transport;
 * [decode] is its inverse.
 */
class EncryptedBlob(val nonce: ByteArray, val ciphertext: ByteArray) {

    fun encode(): String = "${Base64.Default.encode(nonce)}.${Base64.Default.encode(ciphertext)}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedBlob) return false
        return nonce.contentEquals(other.nonce) && ciphertext.contentEquals(other.ciphertext)
    }

    override fun hashCode(): Int = 31 * nonce.contentHashCode() + ciphertext.contentHashCode()

    companion object {
        fun decode(encoded: String): EncryptedBlob {
            val sep = encoded.indexOf('.')
            require(sep > 0 && sep < encoded.length - 1) { "Malformed EncryptedBlob: $encoded" }
            return EncryptedBlob(
                nonce = Base64.Default.decode(encoded.substring(0, sep)),
                ciphertext = Base64.Default.decode(encoded.substring(sep + 1)),
            )
        }
    }
}
