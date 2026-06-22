package com.time.applauncher.goalgaurd.feature.focus.domain

import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.EmptyResult
import kotlinx.coroutines.flow.Flow

interface FocusRepository {
    fun observeTodayFocusMinutes(): Flow<Int>
    suspend fun saveSession(session: FocusSession): EmptyResult<DataError.Local>
}
