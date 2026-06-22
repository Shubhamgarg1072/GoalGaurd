package com.time.applauncher.goalgaurd.backend.insights

import com.time.applauncher.goalgaurd.backend.db.DatabaseFactory.dbQuery
import com.time.applauncher.goalgaurd.backend.db.FocusSessions
import com.time.applauncher.goalgaurd.backend.db.Goals
import com.time.applauncher.goalgaurd.backend.db.HabitLogs
import com.time.applauncher.goalgaurd.backend.db.Habits
import com.time.applauncher.goalgaurd.shared.model.DailyMetricDto
import com.time.applauncher.goalgaurd.shared.model.InsightsSummaryDto
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class InsightsService {

    suspend fun summary(userId: String, from: LocalDate, to: LocalDate): InsightsSummaryDto = dbQuery {
        val fromStr = from.toString()
        val toStr = to.toString()

        // Habit completion rate over the range (fraction of logs marked complete).
        val logs = HabitLogs.selectAll()
            .where { (HabitLogs.userId eq userId) and (HabitLogs.date greaterEq fromStr) and (HabitLogs.date lessEq toStr) }
            .map { it[HabitLogs.isCompleted] }
        val completionRate = if (logs.isEmpty()) 0.0 else logs.count { it }.toDouble() / logs.size

        // Focus minutes by day (filter on the date portion of startedAt).
        val focusByDay = linkedMapOf<String, Int>()
        var totalFocus = 0
        FocusSessions.selectAll()
            .where { (FocusSessions.userId eq userId) and (FocusSessions.deleted eq false) and (FocusSessions.isCompleted eq true) }
            .forEach { row ->
                val day = row[FocusSessions.startedAt].take(10)
                if (day in fromStr..toStr) {
                    val mins = row[FocusSessions.durationMinutes]
                    focusByDay[day] = (focusByDay[day] ?: 0) + mins
                    totalFocus += mins
                }
            }

        val habits = Habits.selectAll()
            .where { (Habits.userId eq userId) and (Habits.deleted eq false) }
            .toList()
        val longestStreak = habits.maxOfOrNull { it[Habits.streak] } ?: 0

        val goals = Goals.selectAll()
            .where { (Goals.userId eq userId) and (Goals.deleted eq false) }
            .toList()
        val goalsOnTrack = goals.count { row ->
            val target = row[Goals.targetValue]
            target > 0 && row[Goals.currentValue] / target >= 0.5
        }

        InsightsSummaryDto(
            from = from,
            to = to,
            habitCompletionRate = completionRate,
            totalFocusMinutes = totalFocus,
            longestStreak = longestStreak,
            goalsOnTrack = goalsOnTrack,
            goalsTotal = goals.size,
            dailyFocusMinutes = focusByDay.entries
                .sortedBy { it.key }
                .map { DailyMetricDto(LocalDate.parse(it.key), it.value) },
        )
    }
}
