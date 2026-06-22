package com.time.applauncher.goalgaurd.feature.habits.data

import com.time.applauncher.goalgaurd.core.database.dao.HabitDao
import com.time.applauncher.goalgaurd.core.database.dao.HabitLogDao
import com.time.applauncher.goalgaurd.core.database.entity.HabitLogEntity
import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.EmptyResult
import com.time.applauncher.goalgaurd.feature.habits.domain.Habit
import com.time.applauncher.goalgaurd.feature.habits.domain.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

class RoomHabitRepository(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
) : HabitRepository {

    override fun observeActiveHabits(): Flow<List<Habit>> =
        habitDao.observeActive().map { entities -> entities.map { it.toHabit() } }

    override fun observeCompletedHabitIdsForDate(date: LocalDate): Flow<Set<String>> =
        habitLogDao.observeByDate(date).map { logs ->
            logs.filter { it.isCompleted }.map { it.habitId }.toSet()
        }

    override suspend fun upsertHabit(habit: Habit): EmptyResult<DataError.Local> = try {
        habitDao.upsert(habit.toHabitEntity())
        com.time.applauncher.goalgaurd.core.domain.Result.Success(Unit)
    } catch (e: Exception) {
        com.time.applauncher.goalgaurd.core.domain.Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun deleteHabit(id: String): EmptyResult<DataError.Local> = try {
        val entity = habitDao.getById(id)
            ?: return com.time.applauncher.goalgaurd.core.domain.Result.Error(DataError.Local.NOT_FOUND)
        habitDao.delete(entity)
        com.time.applauncher.goalgaurd.core.domain.Result.Success(Unit)
    } catch (e: Exception) {
        com.time.applauncher.goalgaurd.core.domain.Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun toggleHabitCompletion(habitId: String, date: LocalDate): EmptyResult<DataError.Local> = try {
        val existing = habitLogDao.getLog(habitId, date)
        val updated = if (existing != null) {
            existing.copy(isCompleted = !existing.isCompleted)
        } else {
            HabitLogEntity(
                id = UUID.randomUUID().toString(),
                habitId = habitId,
                date = date,
                isCompleted = true,
            )
        }
        habitLogDao.upsert(updated)
        com.time.applauncher.goalgaurd.core.domain.Result.Success(Unit)
    } catch (e: Exception) {
        com.time.applauncher.goalgaurd.core.domain.Result.Error(DataError.Local.UNKNOWN)
    }
}
