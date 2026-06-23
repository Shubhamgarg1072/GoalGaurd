package com.time.applauncher.goalgaurd.feature.vault.domain

import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.EmptyResult
import kotlinx.coroutines.flow.Flow

/** Lifecycle of the on-device encryption vault. */
enum class VaultStatus {
    /** No passphrase has ever been set — the encryption key does not exist yet. */
    NOT_SET_UP,

    /** A vault exists but the data-encryption key is not loaded; the app must unlock before reading data. */
    LOCKED,

    /** The DEK is loaded in memory; data can be encrypted/decrypted. */
    UNLOCKED,
}

/**
 * Owns the passphrase → data-encryption-key flow. The DEK never leaves the device unwrapped, and
 * the passphrase is never persisted — so the optional cloud backend stays zero-knowledge.
 */
interface VaultRepository {

    val status: Flow<VaultStatus>

    suspend fun currentStatus(): VaultStatus

    /**
     * First-time setup: generates a random DEK, wraps it under [passphrase], persists the envelope,
     * and unlocks the session. There is intentionally **no recovery** — a forgotten passphrase means
     * the encrypted data is unrecoverable.
     */
    suspend fun setUp(passphrase: String): EmptyResult<DataError>

    /** Unlocks the session. [DataError.Local.WRONG_PASSPHRASE] on a bad passphrase. */
    suspend fun unlock(passphrase: String): EmptyResult<DataError>

    /** Re-wraps the same DEK under a new passphrase (data is not re-encrypted). */
    suspend fun changePassphrase(oldPassphrase: String, newPassphrase: String): EmptyResult<DataError>

    /** Drops the in-memory DEK; the app returns to [VaultStatus.LOCKED]. */
    fun lock()
}
