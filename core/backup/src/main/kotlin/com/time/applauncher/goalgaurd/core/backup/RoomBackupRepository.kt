package com.time.applauncher.goalgaurd.core.backup

import androidx.room.withTransaction
import com.time.applauncher.goalgaurd.core.database.GoalGuardDatabase
import com.time.applauncher.goalgaurd.core.database.dao.BackupDao
import com.time.applauncher.goalgaurd.core.database.dao.GoalDao
import com.time.applauncher.goalgaurd.core.database.dao.HabitDao
import com.time.applauncher.goalgaurd.core.database.dao.HabitLogDao
import com.time.applauncher.goalgaurd.core.database.dao.FocusSessionDao
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

    override suspend fun toJson(bundle: BackupBundle): String =
        json.encodeToString(BackupBundle.serializer(), bundle)

    override suspend fun fromJson(jsonString: String): Result<BackupBundle, DataError> = try {
        Result.Success(json.decodeFromString(BackupBundle.serializer(), jsonString))
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }
}
