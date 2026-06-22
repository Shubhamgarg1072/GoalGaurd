package com.time.applauncher.goalgaurd.feature.focus.domain

import java.time.LocalDateTime

data class FocusSession(
    val id: String,
    val durationMinutes: Int,
    val startedAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val isCompleted: Boolean,
)
