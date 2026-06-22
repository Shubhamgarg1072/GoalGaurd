package com.time.applauncher.goalgaurd.feature.habits.domain

import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.EmptyResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HabitRepository {
    fun observeActiveHabits(): Flow<List<Habit>>
    fun observeCompletedHabitIdsForDate(date: LocalDate): Flow<Set<String>>
    suspend fun upsertHabit(habit: Habit): EmptyResult<DataError.Local>
    suspend fun deleteHabit(id: String): EmptyResult<DataError.Local>
    suspend fun toggleHabitCompletion(habitId: String, date: LocalDate): EmptyResult<DataError.Local>
}
