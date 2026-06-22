package com.time.applauncher.goalgaurd.feature.focus.data

import com.time.applauncher.goalgaurd.core.database.dao.FocusSessionDao
import com.time.applauncher.goalgaurd.core.database.entity.FocusSessionEntity
import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.EmptyResult
import com.time.applauncher.goalgaurd.core.domain.Result
import com.time.applauncher.goalgaurd.feature.focus.domain.FocusRepository
import com.time.applauncher.goalgaurd.feature.focus.domain.FocusSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class RoomFocusRepository(private val dao: FocusSessionDao) : FocusRepository {

    override fun observeTodayFocusMinutes(): Flow<Int> =
        dao.observeTodayFocusMinutes().map { it ?: 0 }

    override suspend fun saveSession(session: FocusSession): EmptyResult<DataError.Local> = try {
        dao.upsert(
            FocusSessionEntity(
                id = session.id,
                durationMinutes = session.durationMinutes,
                startedAt = session.startedAt,
                completedAt = session.completedAt,
                isCompleted = session.isCompleted,
                updatedAt = Instant.now(),
            )
        )
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }
}
