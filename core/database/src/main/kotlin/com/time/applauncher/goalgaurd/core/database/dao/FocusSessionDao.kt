package com.time.applauncher.goalgaurd.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.time.applauncher.goalgaurd.core.database.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface FocusSessionDao {
    @Upsert
    suspend fun upsert(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions WHERE startedAt >= :since ORDER BY startedAt DESC")
    fun observeRecent(since: LocalDateTime): Flow<List<FocusSessionEntity>>

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE isCompleted = 1 AND date(startedAt) = date('now')")
    fun observeTodayFocusMinutes(): Flow<Int?>
}
