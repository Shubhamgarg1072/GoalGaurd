package com.time.applauncher.goalgaurd.feature.vault.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.time.applauncher.goalgaurd.core.crypto.KeyEnvelope
import com.time.applauncher.goalgaurd.core.crypto.VaultCrypto
import com.time.applauncher.goalgaurd.core.crypto.VaultKeyManager
import com.time.applauncher.goalgaurd.core.crypto.WrongPassphraseException
import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.EmptyResult
import com.time.applauncher.goalgaurd.core.domain.Result
import com.time.applauncher.goalgaurd.feature.vault.domain.VaultRepository
import com.time.applauncher.goalgaurd.feature.vault.domain.VaultStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.vaultDataStore: DataStore<Preferences> by preferencesDataStore("vault")

/**
 * Persists only the [KeyEnvelope] (passphrase-wrapped DEK) — never the passphrase or the raw DEK.
 * The unwrapped DEK lives solely in the in-memory [VaultKeyManager] for the unlocked session.
 */
class DataStoreVaultRepository(
    private val context: Context,
    private val keyManager: VaultKeyManager,
) : VaultRepository {

    private val json = Json
    private val unlocked = MutableStateFlow(false)

    private object Keys {
        val ENVELOPE = stringPreferencesKey("key_envelope")
    }

    override val status: Flow<VaultStatus> =
        combine(context.vaultDataStore.data.map { it[Keys.ENVELOPE] != null }, unlocked) { hasEnvelope, isUnlocked ->
            statusOf(hasEnvelope, isUnlocked)
        }

    override suspend fun currentStatus(): VaultStatus =
        statusOf(loadEnvelope() != null, unlocked.value)

    override suspend fun setUp(passphrase: String): EmptyResult<DataError> = try {
        if (loadEnvelope() != null) return Result.Error(DataError.Local.UNKNOWN)
        val (envelope, dek) = VaultCrypto.createEnvelope(passphrase)
        saveEnvelope(envelope)
        keyManager.unlock(dek, envelope)
        unlocked.value = true
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun unlock(passphrase: String): EmptyResult<DataError> = try {
        val envelope = loadEnvelope() ?: return Result.Error(DataError.Local.NOT_FOUND)
        val dek = try {
            VaultCrypto.unwrap(envelope, passphrase)
        } catch (e: WrongPassphraseException) {
            return Result.Error(DataError.Local.WRONG_PASSPHRASE)
        }
        keyManager.unlock(dek, envelope)
        unlocked.value = true
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun changePassphrase(oldPassphrase: String, newPassphrase: String): EmptyResult<DataError> = try {
        val envelope = loadEnvelope() ?: return Result.Error(DataError.Local.NOT_FOUND)
        val rewrapped = try {
            VaultCrypto.rewrap(envelope, oldPassphrase, newPassphrase)
        } catch (e: WrongPassphraseException) {
            return Result.Error(DataError.Local.WRONG_PASSPHRASE)
        }
        saveEnvelope(rewrapped)
        // Keep the session unlocked under the same DEK with the refreshed envelope.
        keyManager.unlock(VaultCrypto.unwrap(rewrapped, newPassphrase), rewrapped)
        unlocked.value = true
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override fun lock() {
        keyManager.lock()
        unlocked.value = false
    }

    private fun statusOf(hasEnvelope: Boolean, isUnlocked: Boolean): VaultStatus = when {
        !hasEnvelope -> VaultStatus.NOT_SET_UP
        isUnlocked -> VaultStatus.UNLOCKED
        else -> VaultStatus.LOCKED
    }

    private suspend fun loadEnvelope(): KeyEnvelope? =
        context.vaultDataStore.data.first()[Keys.ENVELOPE]
            ?.let { runCatching { json.decodeFromString(KeyEnvelope.serializer(), it) }.getOrNull() }

    private suspend fun saveEnvelope(envelope: KeyEnvelope) {
        context.vaultDataStore.edit { it[Keys.ENVELOPE] = json.encodeToString(KeyEnvelope.serializer(), envelope) }
    }
}
