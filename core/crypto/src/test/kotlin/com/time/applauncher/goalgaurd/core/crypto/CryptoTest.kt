package com.time.applauncher.goalgaurd.core.crypto

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEqualTo
import assertk.assertions.isTrue
import kotlinx.serialization.Serializable
import org.junit.Assert.assertThrows
import org.junit.Test
import java.security.GeneralSecurityException

class CryptoTest {

    private val aead = AesGcmAead()

    private inline fun <reified T : Throwable> assertFailsWith(noinline block: () -> Unit) {
        assertThrows(T::class.java, block)
    }

    // ── AES-GCM ───────────────────────────────────────────────────────────────

    @Test
    fun `aes-gcm round trips`() {
        val key = VaultCrypto.newDek()
        val plaintext = "hydrate, then run 5k".encodeToByteArray()
        val blob = aead.encrypt(plaintext, key)
        assertThat(aead.decrypt(blob, key).decodeToString()).isEqualTo("hydrate, then run 5k")
    }

    @Test
    fun `aes-gcm rejects a tampered ciphertext`() {
        val key = VaultCrypto.newDek()
        val blob = aead.encrypt("secret".encodeToByteArray(), key)
        blob.ciphertext[0] = (blob.ciphertext[0].toInt() xor 0x01).toByte()
        assertFailsWith<GeneralSecurityException> { aead.decrypt(blob, key) }
    }

    @Test
    fun `aes-gcm rejects a wrong key`() {
        val blob = aead.encrypt("secret".encodeToByteArray(), VaultCrypto.newDek())
        assertFailsWith<GeneralSecurityException> { aead.decrypt(blob, VaultCrypto.newDek()) }
    }

    @Test
    fun `encrypting the same plaintext twice yields different nonces`() {
        val key = VaultCrypto.newDek()
        val a = aead.encrypt("x".encodeToByteArray(), key)
        val b = aead.encrypt("x".encodeToByteArray(), key)
        assertThat(a.nonce.toHex()).isNotEqualTo(b.nonce.toHex())
    }

    // ── EncryptedBlob codec ─────────────────────────────────────────────────────

    @Test
    fun `encrypted blob encodes and decodes`() {
        val key = VaultCrypto.newDek()
        val blob = aead.encrypt("payload".encodeToByteArray(), key)
        val restored = EncryptedBlob.decode(blob.encode())
        assertThat(restored).isEqualTo(blob)
        assertThat(aead.decrypt(restored, key).decodeToString()).isEqualTo("payload")
    }

    // ── PBKDF2 (RFC 2898, HMAC-SHA256 vectors) ──────────────────────────────────

    @Test
    fun `pbkdf2 matches known vectors`() {
        val c1 = Pbkdf2.deriveKey("password", "salt".encodeToByteArray(), iterations = 1, keyLenBytes = 32)
        assertThat(c1.toHex())
            .isEqualTo("120fb6cffcf8b32c43e7225256c4f837a86548c92ccc35480805987cb70be17b")

        val c2 = Pbkdf2.deriveKey("password", "salt".encodeToByteArray(), iterations = 2, keyLenBytes = 32)
        assertThat(c2.toHex())
            .isEqualTo("ae4d0c95af6b46d32d0adff928f06dd02a303f8ef3c251dfd6e2d85a95474c43")
    }

    // ── KeyEnvelope (passphrase wrap/unwrap) ────────────────────────────────────

    @Test
    fun `envelope unwraps with the correct passphrase`() {
        val (envelope, dek) = VaultCrypto.createEnvelope("correct horse", iterations = 1_000)
        val recovered = VaultCrypto.unwrap(envelope, "correct horse")
        assertThat(recovered.toHex()).isEqualTo(dek.toHex())
    }

    @Test
    fun `envelope rejects the wrong passphrase`() {
        val (envelope, _) = VaultCrypto.createEnvelope("correct horse", iterations = 1_000)
        assertFailsWith<WrongPassphraseException> { VaultCrypto.unwrap(envelope, "battery staple") }
    }

    @Test
    fun `rewrap keeps the same dek and swaps the passphrase`() {
        val (envelope, dek) = VaultCrypto.createEnvelope("old-pass", iterations = 1_000)
        val rewrapped = VaultCrypto.rewrap(envelope, "old-pass", "new-pass")

        assertThat(VaultCrypto.unwrap(rewrapped, "new-pass").toHex()).isEqualTo(dek.toHex())
        assertFailsWith<WrongPassphraseException> { VaultCrypto.unwrap(rewrapped, "old-pass") }
    }

    // ── BlindIndex ──────────────────────────────────────────────────────────────

    @Test
    fun `blind index is deterministic per dek and distinguishes values`() {
        val dek = VaultCrypto.newDek()
        val indexKey = BlindIndex.deriveIndexKey(dek)
        val a1 = BlindIndex.compute(indexKey, "habit-1|2026-06-22")
        val a2 = BlindIndex.compute(indexKey, "habit-1|2026-06-22")
        val b = BlindIndex.compute(indexKey, "habit-1|2026-06-23")

        assertThat(a1).isEqualTo(a2)
        assertThat(a1).isNotEqualTo(b)
        // A different DEK yields a different index for the same value.
        val other = BlindIndex.compute(BlindIndex.deriveIndexKey(VaultCrypto.newDek()), "habit-1|2026-06-22")
        assertThat(a1).isNotEqualTo(other)
    }

    // ── JsonCryptoCodec ─────────────────────────────────────────────────────────

    @Serializable
    private data class Sample(val id: String, val n: Int)

    @Test
    fun `json codec round trips an object`() {
        val codec = JsonCryptoCodec()
        val dek = VaultCrypto.newDek()
        val value = Sample("g1", 42)
        val blob = codec.encrypt(Sample.serializer(), value, dek)
        assertThat(codec.decrypt(Sample.serializer(), blob, dek)).isEqualTo(value)
    }

    // ── VaultKeyManager ──────────────────────────────────────────────────────────

    @Test
    fun `key manager unlocks and locks`() {
        val vault = InMemoryVaultKeyManager()
        assertThat(vault.isUnlocked).isFalse()
        assertFailsWith<VaultLockedException> { vault.requireDek() }

        val (envelope, dek) = VaultCrypto.createEnvelope("pass", iterations = 1_000)
        vault.unlock(dek, envelope)
        assertThat(vault.isUnlocked).isTrue()
        assertThat(vault.requireDek().toHex()).isEqualTo(dek.toHex())
        assertThat(vault.currentEnvelope()).isEqualTo(envelope)

        vault.lock()
        assertThat(vault.isUnlocked).isFalse()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
