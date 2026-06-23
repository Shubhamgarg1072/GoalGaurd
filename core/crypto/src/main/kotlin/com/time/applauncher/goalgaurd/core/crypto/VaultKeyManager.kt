package com.time.applauncher.goalgaurd.core.crypto

/**
 * Holds the unlocked data-encryption key (DEK) in memory for the lifetime of an unlocked session.
 * Everything that touches user data — Room (SQLCipher), backup files, cloud sync — reads the DEK
 * from here, so locking ([lock]) instantly cuts off all decryption.
 *
 * The DEK itself is recovered from a [KeyEnvelope] + passphrase via [VaultCrypto.unwrap]; persisting
 * the envelope and caching the DEK behind device credentials is the data layer's job (Android
 * Keystore lives in `:feature:vault:data`), keeping this type pure and unit-testable.
 */
interface VaultKeyManager {
    val isUnlocked: Boolean

    /** The DEK if unlocked, else null. */
    fun dekOrNull(): ByteArray?

    /** The DEK, or throws [VaultLockedException] if locked. */
    fun requireDek(): ByteArray

    /** The envelope the active DEK was unwrapped from, if unlocked. Embedded in exported backups. */
    fun currentEnvelope(): KeyEnvelope?

    /** Loads a recovered DEK (and the envelope it came from) into memory, unlocking the session. */
    fun unlock(dek: ByteArray, envelope: KeyEnvelope)

    /** Zeroes and drops the in-memory DEK. */
    fun lock()
}

class InMemoryVaultKeyManager : VaultKeyManager {

    @Volatile
    private var dek: ByteArray? = null

    @Volatile
    private var envelope: KeyEnvelope? = null

    override val isUnlocked: Boolean get() = dek != null

    override fun dekOrNull(): ByteArray? = dek

    override fun requireDek(): ByteArray = dek ?: throw VaultLockedException()

    override fun currentEnvelope(): KeyEnvelope? = envelope

    override fun unlock(dek: ByteArray, envelope: KeyEnvelope) {
        this.dek = dek.copyOf()
        this.envelope = envelope
    }

    override fun lock() {
        dek?.fill(0)
        dek = null
        envelope = null
    }
}
