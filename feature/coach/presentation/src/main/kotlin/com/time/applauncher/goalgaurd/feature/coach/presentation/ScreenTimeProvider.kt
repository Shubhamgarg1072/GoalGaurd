package com.time.applauncher.goalgaurd.feature.coach.presentation

interface ScreenTimeProvider {
    suspend fun getTodaySocialMinutes(): Int
}

class NoopScreenTimeProvider : ScreenTimeProvider {
    override suspend fun getTodaySocialMinutes(): Int = 0
}
