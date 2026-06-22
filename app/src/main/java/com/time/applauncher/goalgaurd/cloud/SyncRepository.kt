package com.time.applauncher.goalgaurd.cloud

import com.time.applauncher.goalgaurd.core.domain.BackupRepository
import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.EmptyResult
import com.time.applauncher.goalgaurd.core.domain.Result
import com.time.applauncher.goalgaurd.shared.api.GoalGuardApi
import com.time.applauncher.goalgaurd.shared.model.SyncRequest
import com.time.applauncher.goalgaurd.shared.util.Result as SharedResult

/**
 * Server-backed sync built on top of the existing local [BackupRepository]: export the local
 * bundle, push it to `/sync`, and merge the server's response back in (last-write-wins on the
 * server). Strictly optional — any failure leaves local data untouched.
 */
class SyncRepository(
    private val api: GoalGuardApi,
    private val backupRepository: BackupRepository,
) {
    suspend fun syncNow(): EmptyResult<DataError> {
        val bundle = when (val export = backupRepository.export()) {
            is Result.Success -> export.data
            is Result.Error -> return Result.Error(export.error)
        }
        val request = SyncRequest(since = null, changes = bundle.toSyncPayload())
        return when (val response = api.sync(request)) {
            is SharedResult.Success -> backupRepository.importBundle(response.data.changes.toBackupBundle())
            is SharedResult.Error -> Result.Error(response.error.toDataErrorNetwork())
        }
    }
}

private fun com.time.applauncher.goalgaurd.shared.util.NetworkError.toDataErrorNetwork(): DataError =
    runCatching { DataError.Network.valueOf(name) }.getOrDefault(DataError.Network.UNKNOWN)
