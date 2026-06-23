package com.time.applauncher.goalgaurd.cloud

import com.time.applauncher.goalgaurd.core.crypto.JsonCryptoCodec
import com.time.applauncher.goalgaurd.core.crypto.VaultKeyManager
import com.time.applauncher.goalgaurd.core.domain.BackupRepository
import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.EmptyResult
import com.time.applauncher.goalgaurd.core.domain.Result
import com.time.applauncher.goalgaurd.shared.api.GoalGuardApi
import com.time.applauncher.goalgaurd.shared.model.SyncRequest
import com.time.applauncher.goalgaurd.shared.util.Result as SharedResult

/**
 * End-to-end-encrypted server sync on top of the local [BackupRepository]: export the local bundle,
 * **encrypt every record under the vault DEK**, push to `/sync`, then decrypt and merge the server's
 * response back in. The backend only ever sees ciphertext. Strictly optional and gated on an
 * unlocked vault — any failure (including a locked vault) leaves local data untouched.
 */
class SyncRepository(
    private val api: GoalGuardApi,
    private val backupRepository: BackupRepository,
    private val vaultKeyManager: VaultKeyManager,
    private val codec: JsonCryptoCodec = JsonCryptoCodec(),
) {
    suspend fun syncNow(): EmptyResult<DataError> {
        val dek = vaultKeyManager.dekOrNull() ?: return Result.Error(DataError.Local.VAULT_LOCKED)
        val bundle = when (val export = backupRepository.export()) {
            is Result.Success -> export.data
            is Result.Error -> return Result.Error(export.error)
        }
        val request = SyncRequest(since = null, changes = bundle.toEncryptedSyncPayload(dek, codec))
        return when (val response = api.sync(request)) {
            is SharedResult.Success ->
                backupRepository.importBundle(response.data.changes.toBackupBundle(dek, codec))
            is SharedResult.Error -> Result.Error(response.error.toDataErrorNetwork())
        }
    }
}

private fun com.time.applauncher.goalgaurd.shared.util.NetworkError.toDataErrorNetwork(): DataError =
    runCatching { DataError.Network.valueOf(name) }.getOrDefault(DataError.Network.UNKNOWN)
