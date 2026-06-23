package com.time.applauncher.goalgaurd.core.crypto

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64

/**
 * Deterministic keyed hash used to let the server enforce uniqueness/union on encrypted habit logs
 * (per `habitId|date`) without ever learning the values. The index key is derived from the DEK, so
 * the same DEK always produces the same index — but it is unrelated to the encryption nonce/key and
 * reveals nothing without the DEK.
 */
object BlindIndex {

    private const val HMAC = "HmacSHA256"
    private const val INDEX_KEY_INFO = "goalguard-blind-index-v1"

    /** Derives a stable index key from the DEK (separate from the AEAD usage of the DEK). */
    fun deriveIndexKey(dek: ByteArray): ByteArray = hmac(dek, INDEX_KEY_INFO.encodeToByteArray())

    /** The opaque, server-storable index for [value] (e.g. "habitId|2026-06-22"). */
    fun compute(indexKey: ByteArray, value: String): String =
        Base64.Default.encode(hmac(indexKey, value.encodeToByteArray()))

    private fun hmac(key: ByteArray, data: ByteArray): ByteArray =
        Mac.getInstance(HMAC).apply { init(SecretKeySpec(key, HMAC)) }.doFinal(data)
}
