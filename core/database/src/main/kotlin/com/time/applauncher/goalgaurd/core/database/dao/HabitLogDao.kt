package com.time.applauncher.goalgaurd.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.time.applauncher.goalgaurd.core.database.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitLogDao {
    @Upsert
    suspend fun upsert(log: HabitLogEntity)

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun observeByDate(date: LocalDate): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC LIMIT 30")
    suspend fun getRecentLogs(habitId: String): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getLog(habitId: String, date: LocalDate): HabitLogEntity?
}
