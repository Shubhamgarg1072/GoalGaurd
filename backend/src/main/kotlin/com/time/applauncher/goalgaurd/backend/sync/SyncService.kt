package com.time.applauncher.goalgaurd.backend.sync

import com.time.applauncher.goalgaurd.backend.db.DatabaseFactory.dbQuery
import com.time.applauncher.goalgaurd.backend.db.FocusSessions
import com.time.applauncher.goalgaurd.backend.db.Goals
import com.time.applauncher.goalgaurd.backend.db.HabitLogs
import com.time.applauncher.goalgaurd.backend.db.Habits
import com.time.applauncher.goalgaurd.shared.model.FocusSessionSyncDto
import com.time.applauncher.goalgaurd.shared.model.GoalSyncDto
import com.time.applauncher.goalgaurd.shared.model.HabitLogSyncDto
import com.time.applauncher.goalgaurd.shared.model.HabitSyncDto
import com.time.applauncher.goalgaurd.shared.model.SyncPayload
import com.time.applauncher.goalgaurd.shared.model.SyncRequest
import com.time.applauncher.goalgaurd.shared.model.SyncResponse
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import java.time.Instant as JInstant

/**
 * Server-backed last-write-wins sync. Goals/habits/focus_sessions merge by newest [updatedAt];
 * habit_logs are append-only and merged by union (insert-or-ignore on user+habit+date).
 */
class SyncService {

    suspend fun sync(userId: String, request: SyncRequest): SyncResponse = dbQuery {
        mergeGoals(userId, request.changes.goals)
        mergeHabits(userId, request.changes.habits)
        mergeFocusSessions(userId, request.changes.focusSessions)
        mergeHabitLogs(userId, request.changes.habitLogs)

        val since = request.since?.toJavaInstant()
        SyncResponse(
            serverTime = JInstant.now().toKotlinInstant(),
            changes = SyncPayload(
                goals = pullGoals(userId, since),
                habits = pullHabits(userId, since),
                focusSessions = pullFocusSessions(userId, since),
                habitLogs = pullHabitLogs(userId),
            ),
        )
    }

    // ── merge (push) ──────────────────────────────────────────────────────────

    private fun mergeGoals(userId: String, incoming: List<GoalSyncDto>) {
        for (dto in incoming) {
            val existing = Goals.selectAll()
                .where { (Goals.id eq dto.id) and (Goals.userId eq userId) }
                .singleOrNull()?.get(Goals.updatedAt)
            if (existing != null && existing >= dto.updatedAt.toJavaInstant()) continue
            Goals.upsert(Goals.id) {
                it[id] = dto.id
                it[Goals.userId] = userId
                it[name] = dto.name
                it[emoji] = dto.emoji
                it[targetValue] = dto.targetValue
                it[currentValue] = dto.currentValue
                it[unit] = dto.unit
                it[targetDate] = dto.targetDate.toString()
                it[priority] = dto.priority
                it[createdAt] = dto.createdAt.toString()
                it[updatedAt] = dto.updatedAt.toJavaInstant()
                it[deleted] = dto.deleted
            }
        }
    }

    private fun mergeHabits(userId: String, incoming: List<HabitSyncDto>) {
        for (dto in incoming) {
            val existing = Habits.selectAll()
                .where { (Habits.id eq dto.id) and (Habits.userId eq userId) }
                .singleOrNull()?.get(Habits.updatedAt)
            if (existing != null && existing >= dto.updatedAt.toJavaInstant()) continue
            Habits.upsert(Habits.id) {
                it[id] = dto.id
                it[Habits.userId] = userId
                it[goalId] = dto.goalId
                it[name] = dto.name
                it[emoji] = dto.emoji
                it[frequency] = dto.frequency
                it[difficulty] = dto.difficulty
                it[reminderTime] = dto.reminderTime
                it[streak] = dto.streak
                it[isActive] = dto.isActive
                it[updatedAt] = dto.updatedAt.toJavaInstant()
                it[deleted] = dto.deleted
            }
        }
    }

    private fun mergeFocusSessions(userId: String, incoming: List<FocusSessionSyncDto>) {
        for (dto in incoming) {
            val existing = FocusSessions.selectAll()
                .where { (FocusSessions.id eq dto.id) and (FocusSessions.userId eq userId) }
                .singleOrNull()?.get(FocusSessions.updatedAt)
            if (existing != null && existing >= dto.updatedAt.toJavaInstant()) continue
            FocusSessions.upsert(FocusSessions.id) {
                it[id] = dto.id
                it[FocusSessions.userId] = userId
                it[durationMinutes] = dto.durationMinutes
                it[startedAt] = dto.startedAt.toString()
                it[completedAt] = dto.completedAt?.toString()
                it[isCompleted] = dto.isCompleted
                it[updatedAt] = dto.updatedAt.toJavaInstant()
                it[deleted] = dto.deleted
            }
        }
    }

    private fun mergeHabitLogs(userId: String, incoming: List<HabitLogSyncDto>) {
        for (dto in incoming) {
            HabitLogs.insertIgnore {
                it[id] = dto.id
                it[HabitLogs.userId] = userId
                it[habitId] = dto.habitId
                it[date] = dto.date.toString()
                it[isCompleted] = dto.isCompleted
            }
        }
    }

    // ── pull ────────────────────────────────────────────────────────────────

    private fun pullGoals(userId: String, since: JInstant?): List<GoalSyncDto> =
        Goals.selectAll()
            .where { (Goals.userId eq userId).let { c -> if (since != null) c and (Goals.updatedAt greater since) else c } }
            .map { it.toGoalDto() }

    private fun pullHabits(userId: String, since: JInstant?): List<HabitSyncDto> =
        Habits.selectAll()
            .where { (Habits.userId eq userId).let { c -> if (since != null) c and (Habits.updatedAt greater since) else c } }
            .map { it.toHabitDto() }

    private fun pullFocusSessions(userId: String, since: JInstant?): List<FocusSessionSyncDto> =
        FocusSessions.selectAll()
            .where { (FocusSessions.userId eq userId).let { c -> if (since != null) c and (FocusSessions.updatedAt greater since) else c } }
            .map { it.toFocusDto() }

    private fun pullHabitLogs(userId: String): List<HabitLogSyncDto> =
        HabitLogs.selectAll().where { HabitLogs.userId eq userId }.map { it.toHabitLogDto() }

    // ── row mappers ───────────────────────────────────────────────────────────

    private fun ResultRow.toGoalDto() = GoalSyncDto(
        id = this[Goals.id],
        name = this[Goals.name],
        emoji = this[Goals.emoji],
        targetValue = this[Goals.targetValue],
        currentValue = this[Goals.currentValue],
        unit = this[Goals.unit],
        targetDate = LocalDate.parse(this[Goals.targetDate]),
        priority = this[Goals.priority],
        createdAt = LocalDate.parse(this[Goals.createdAt]),
        updatedAt = this[Goals.updatedAt].toKotlinInstant(),
        deleted = this[Goals.deleted],
    )

    private fun ResultRow.toHabitDto() = HabitSyncDto(
        id = this[Habits.id],
        goalId = this[Habits.goalId],
        name = this[Habits.name],
        emoji = this[Habits.emoji],
        frequency = this[Habits.frequency],
        difficulty = this[Habits.difficulty],
        reminderTime = this[Habits.reminderTime],
        streak = this[Habits.streak],
        isActive = this[Habits.isActive],
        updatedAt = this[Habits.updatedAt].toKotlinInstant(),
        deleted = this[Habits.deleted],
    )

    private fun ResultRow.toFocusDto() = FocusSessionSyncDto(
        id = this[FocusSessions.id],
        durationMinutes = this[FocusSessions.durationMinutes],
        startedAt = LocalDateTime.parse(this[FocusSessions.startedAt]),
        completedAt = this[FocusSessions.completedAt]?.let { LocalDateTime.parse(it) },
        isCompleted = this[FocusSessions.isCompleted],
        updatedAt = this[FocusSessions.updatedAt].toKotlinInstant(),
        deleted = this[FocusSessions.deleted],
    )

    private fun ResultRow.toHabitLogDto() = HabitLogSyncDto(
        id = this[HabitLogs.id],
        habitId = this[HabitLogs.habitId],
        date = LocalDate.parse(this[HabitLogs.date]),
        isCompleted = this[HabitLogs.isCompleted],
    )
}
