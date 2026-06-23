package com.time.applauncher.goalgaurd.core.backup

import androidx.room.withTransaction
import com.time.applauncher.goalgaurd.core.database.GoalGuardDatabase
import com.time.applauncher.goalgaurd.core.database.dao.BackupDao
import com.time.applauncher.goalgaurd.core.database.dao.GoalDao
import com.time.applauncher.goalgaurd.core.database.dao.HabitDao
import com.time.applauncher.goalgaurd.core.database.dao.HabitLogDao
import com.time.applauncher.goalgaurd.core.database.dao.FocusSessionDao
import com.time.applauncher.goalgaurd.core.crypto.EncryptedBlob
import com.time.applauncher.goalgaurd.core.crypto.JsonCryptoCodec
import com.time.applauncher.goalgaurd.core.crypto.VaultCrypto
import com.time.applauncher.goalgaurd.core.crypto.VaultKeyManager
import com.time.applauncher.goalgaurd.core.crypto.WrongPassphraseException
import com.time.applauncher.goalgaurd.core.domain.BackupBundle
import com.time.applauncher.goalgaurd.core.domain.BackupRepository
import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.EmptyResult
import com.time.applauncher.goalgaurd.core.domain.Result
import kotlinx.serialization.json.Json
import java.time.Instant

class RoomBackupRepository(
    private val db: GoalGuardDatabase,
    private val backupDao: BackupDao,
    private val goalDao: GoalDao,
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val focusSessionDao: FocusSessionDao,
    private val vaultKeyManager: VaultKeyManager,
    private val codec: JsonCryptoCodec = JsonCryptoCodec(),
) : BackupRepository {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    override suspend fun export(): Result<BackupBundle, DataError> = try {
        val bundle = BackupBundle(
            createdAt = Instant.now(),
            goals = backupDao.getAllGoals().map { it.toDto() },
            habits = backupDao.getAllHabits().map { it.toDto() },
            habitLogs = backupDao.getAllHabitLogs().map { it.toDto() },
            focusSessions = backupDao.getAllFocusSessions().map { it.toDto() },
        )
        Result.Success(bundle)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun importBundle(bundle: BackupBundle): EmptyResult<DataError> = try {
        db.withTransaction {
            // Goals — LWW: replace only if incoming row is newer
            for (dto in bundle.goals) {
                val existing = goalDao.getById(dto.id)
                if (existing == null || dto.updatedAt > existing.updatedAt) {
                    goalDao.upsert(dto.toEntity())
                }
            }
            // Habits — LWW
            for (dto in bundle.habits) {
                val existing = habitDao.getById(dto.id)
                if (existing == null || dto.updatedAt > existing.updatedAt) {
                    habitDao.upsert(dto.toEntity())
                }
            }
            // Focus sessions — LWW
            for (dto in bundle.focusSessions) {
                val existing = backupDao.getFocusSessionById(dto.id)
                if (existing == null || dto.updatedAt > existing.updatedAt) {
                    focusSessionDao.upsert(dto.toEntity())
                }
            }
            // Habit logs — union: insert only if no log exists for that (habitId, date) pair
            for (dto in bundle.habitLogs) {
                val existing = habitLogDao.getLog(dto.habitId, dto.date)
                if (existing == null) {
                    habitLogDao.upsert(dto.toEntity())
                }
            }
        }
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun toJson(bundle: BackupBundle): Result<String, DataError> = try {
        val dek = vaultKeyManager.dekOrNull() ?: return Result.Error(DataError.Local.VAULT_LOCKED)
        val envelope = vaultKeyManager.currentEnvelope() ?: return Result.Error(DataError.Local.VAULT_LOCKED)
        val encrypted = EncryptedBackup(
            envelope = envelope,
            data = codec.encrypt(BackupBundle.serializer(), bundle, dek).encode(),
        )
        Result.Success(json.encodeToString(EncryptedBackup.serializer(), encrypted))
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun fromJson(jsonString: String, passphrase: String?): Result<BackupBundle, DataError> = try {
        val encrypted = runCatching { json.decodeFromString(EncryptedBackup.serializer(), jsonString) }.getOrNull()
        if (encrypted == null) {
            // Legacy, unencrypted backup file.
            Result.Success(json.decodeFromString(BackupBundle.serializer(), jsonString))
        } else {
            val dek = when {
                passphrase != null -> try {
                    VaultCrypto.unwrap(encrypted.envelope, passphrase)
                } catch (e: WrongPassphraseException) {
                    return Result.Error(DataError.Local.WRONG_PASSPHRASE)
                }
                else -> vaultKeyManager.dekOrNull() ?: return Result.Error(DataError.Local.ENCRYPTED_NEEDS_PASSPHRASE)
            }
            val bundle = try {
                codec.decrypt(BackupBundle.serializer(), EncryptedBlob.decode(encrypted.data), dek)
            } catch (e: Exception) {
                // In-memory DEK belongs to a different vault than this file — ask for the passphrase.
                return Result.Error(
                    if (passphrase == null) DataError.Local.ENCRYPTED_NEEDS_PASSPHRASE
                    else DataError.Local.WRONG_PASSPHRASE
                )
            }
            Result.Success(bundle)
        }
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }
}
