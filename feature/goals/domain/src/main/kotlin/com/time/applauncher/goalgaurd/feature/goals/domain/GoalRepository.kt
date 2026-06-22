package com.time.applauncher.goalgaurd.feature.goals.domain

import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.EmptyResult
import com.time.applauncher.goalgaurd.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeGoals(): Flow<List<Goal>>
    suspend fun getGoal(id: String): Result<Goal, DataError.Local>
    suspend fun upsertGoal(goal: Goal): EmptyResult<DataError.Local>
    suspend fun deleteGoal(id: String): EmptyResult<DataError.Local>
    suspend fun updateProgress(goalId: String, newValue: Double): EmptyResult<DataError.Local>
}
