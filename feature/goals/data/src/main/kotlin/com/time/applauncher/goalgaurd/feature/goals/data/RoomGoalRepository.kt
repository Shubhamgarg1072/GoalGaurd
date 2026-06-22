package com.time.applauncher.goalgaurd.feature.goals.data

import com.time.applauncher.goalgaurd.core.database.dao.GoalDao
import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.EmptyResult
import com.time.applauncher.goalgaurd.core.domain.Result
import com.time.applauncher.goalgaurd.feature.goals.domain.Goal
import com.time.applauncher.goalgaurd.feature.goals.domain.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class RoomGoalRepository(private val dao: GoalDao) : GoalRepository {

    override fun observeGoals(): Flow<List<Goal>> =
        dao.observeAll().map { entities -> entities.map { it.toGoal() } }

    override suspend fun getGoal(id: String): Result<Goal, DataError.Local> = try {
        val entity = dao.getById(id)
        if (entity != null) Result.Success(entity.toGoal())
        else Result.Error(DataError.Local.NOT_FOUND)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun upsertGoal(goal: Goal): EmptyResult<DataError.Local> = try {
        dao.upsert(goal.toGoalEntity())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun deleteGoal(id: String): EmptyResult<DataError.Local> = try {
        val entity = dao.getById(id) ?: return Result.Error(DataError.Local.NOT_FOUND)
        dao.delete(entity)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun updateProgress(goalId: String, newValue: Double): EmptyResult<DataError.Local> = try {
        val entity = dao.getById(goalId) ?: return Result.Error(DataError.Local.NOT_FOUND)
        dao.upsert(entity.copy(currentValue = newValue, updatedAt = Instant.now()))
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }
}
