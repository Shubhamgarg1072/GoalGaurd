package com.time.applauncher.goalgaurd.core.crypto

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * PBKDF2-HMAC-SHA256 implemented directly on the platform [Mac] (HmacSHA256), which is present on
 * every Android API level. (The `PBKDF2WithHmacSHA256` `SecretKeyFactory` is only guaranteed from
 * API 26, and minSdk here is 24, so we drive the HMAC ourselves.) The construction follows RFC 2898.
 */
object Pbkdf2 {

    private const val HMAC = "HmacSHA256"
    private const val HASH_LEN = 32

    /** Derives a [keyLenBytes]-byte key from [passphrase] (UTF-8) and [salt] over [iterations] rounds. */
    fun deriveKey(passphrase: String, salt: ByteArray, iterations: Int, keyLenBytes: Int = 32): ByteArray {
        require(iterations > 0) { "iterations must be positive" }
        require(keyLenBytes > 0) { "keyLenBytes must be positive" }

        val mac = Mac.getInstance(HMAC).apply {
            init(SecretKeySpec(passphrase.encodeToByteArray(), HMAC))
        }

        val blocks = (keyLenBytes + HASH_LEN - 1) / HASH_LEN
        val output = ByteArray(blocks * HASH_LEN)
        for (i in 1..blocks) {
            val block = deriveBlock(mac, salt, iterations, i)
            block.copyInto(output, destinationOffset = (i - 1) * HASH_LEN)
        }
        return output.copyOf(keyLenBytes)
    }

    /** F(P, S, c, i) = U1 xor U2 xor ... xor Uc, with U1 = PRF(S || INT(i)). */
    private fun deriveBlock(mac: Mac, salt: ByteArray, iterations: Int, blockIndex: Int): ByteArray {
        var u = mac.run {
            update(salt)
            update(intToBytes(blockIndex))
            doFinal()
        }
        val result = u.copyOf()
        for (round in 2..iterations) {
            u = mac.doFinal(u)
            for (k in result.indices) result[k] = (result[k].toInt() xor u[k].toInt()).toByte()
        }
        return result
    }

    private fun intToBytes(value: Int): ByteArray = byteArrayOf(
        (value ushr 24).toByte(),
        (value ushr 16).toByte(),
        (value ushr 8).toByte(),
        value.toByte(),
    )
}
