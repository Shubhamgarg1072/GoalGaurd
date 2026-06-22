package com.time.applauncher.goalgaurd.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.time.applauncher.goalgaurd.core.database.entity.FocusSessionEntity
import com.time.applauncher.goalgaurd.core.database.entity.GoalEntity
import com.time.applauncher.goalgaurd.core.database.entity.HabitEntity
import com.time.applauncher.goalgaurd.core.database.entity.HabitLogEntity

@Dao
interface BackupDao {
    @Query("SELECT * FROM goals")
    suspend fun getAllGoals(): List<GoalEntity>

    @Query("SELECT * FROM habits")
    suspend fun getAllHabits(): List<HabitEntity>

    @Query("SELECT * FROM habit_logs")
    suspend fun getAllHabitLogs(): List<HabitLogEntity>

    @Query("SELECT * FROM focus_sessions")
    suspend fun getAllFocusSessions(): List<FocusSessionEntity>

    @Query("SELECT * FROM focus_sessions WHERE id = :id")
    suspend fun getFocusSessionById(id: String): FocusSessionEntity?
}
