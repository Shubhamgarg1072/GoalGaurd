package com.time.applauncher.goalgaurd.shared.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class DailyMetricDto(
    val date: LocalDate,
    val value: Int,
)

@Serializable
data class InsightsSummaryDto(
    val from: LocalDate,
    val to: LocalDate,
    val habitCompletionRate: Double,
    val totalFocusMinutes: Int,
    val longestStreak: Int,
    val goalsOnTrack: Int,
    val goalsTotal: Int,
    val dailyFocusMinutes: List<DailyMetricDto> = emptyList(),
)
