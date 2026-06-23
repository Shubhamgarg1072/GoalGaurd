package com.time.applauncher.goalgaurd.core.crypto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * Encrypts/decrypts `@Serializable` objects: serialise to JSON, then AES-GCM under the DEK. Used by
 * both the encrypted sync payloads and the encrypted backup file. The resulting [EncryptedBlob] is
 * opaque — only a holder of the DEK can recover the object.
 */
class JsonCryptoCodec(
    private val aead: Aead = AesGcmAead(),
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
) {
    fun <T> encrypt(serializer: KSerializer<T>, value: T, dek: ByteArray): EncryptedBlob =
        aead.encrypt(json.encodeToString(serializer, value).encodeToByteArray(), dek)

    fun <T> decrypt(serializer: KSerializer<T>, blob: EncryptedBlob, dek: ByteArray): T =
        json.decodeFromString(serializer, aead.decrypt(blob, dek).decodeToString())
}
