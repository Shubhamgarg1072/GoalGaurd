package com.time.applauncher.goalgaurd.backend.sync

import com.time.applauncher.goalgaurd.backend.db.DatabaseFactory.dbQuery
import com.time.applauncher.goalgaurd.backend.db.FocusSessions
import com.time.applauncher.goalgaurd.backend.db.Goals
import com.time.applauncher.goalgaurd.backend.db.HabitLogs
import com.time.applauncher.goalgaurd.backend.db.Habits
import com.time.applauncher.goalgaurd.shared.model.EncryptedLogDto
import com.time.applauncher.goalgaurd.shared.model.EncryptedRecordDto
import com.time.applauncher.goalgaurd.shared.model.SyncPayload
import com.time.applauncher.goalgaurd.shared.model.SyncRequest
import com.time.applauncher.goalgaurd.shared.model.SyncResponse
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import org.jetbrains.exposed.sql.Column
import java.time.Instant as JInstant

/**
 * Server-backed last-write-wins sync over **encrypted** records. The server merges purely on the
 * cleartext metadata (id / updatedAt / deleted, or a blind dedupe index for logs) and stores the
 * opaque ciphertext blob untouched — it never decrypts anything. Goals/habits/focus_sessions merge
 * by newest updatedAt; habit_logs are append-only, merged by union on (user, dedupeKey).
 */
class SyncService {

    suspend fun sync(userId: String, request: SyncRequest): SyncResponse = dbQuery {
        mergeRecords(Goals, GoalsCols, userId, request.changes.goals)
        mergeRecords(Habits, HabitsCols, userId, request.changes.habits)
        mergeRecords(FocusSessions, FocusCols, userId, request.changes.focusSessions)
        mergeHabitLogs(userId, request.changes.habitLogs)

        val since = request.since?.toJavaInstant()
        SyncResponse(
            serverTime = JInstant.now().toKotlinInstant(),
            changes = SyncPayload(
                goals = pullRecords(Goals, GoalsCols, userId, since),
                habits = pullRecords(Habits, HabitsCols, userId, since),
                focusSessions = pullRecords(FocusSessions, FocusCols, userId, since),
                habitLogs = pullHabitLogs(userId),
            ),
        )
    }

    // The three record tables share an identical shape; this bundles their columns so the merge/pull
    // logic can be written once.
    private class RecordCols(
        val id: Column<String>,
        val userId: Column<String>,
        val blob: Column<String>,
        val updatedAt: Column<JInstant>,
        val deleted: Column<Boolean>,
    )

    private val GoalsCols = RecordCols(Goals.id, Goals.userId, Goals.blob, Goals.updatedAt, Goals.deleted)
    private val HabitsCols = RecordCols(Habits.id, Habits.userId, Habits.blob, Habits.updatedAt, Habits.deleted)
    private val FocusCols = RecordCols(FocusSessions.id, FocusSessions.userId, FocusSessions.blob, FocusSessions.updatedAt, FocusSessions.deleted)

    // ── merge (push) ──────────────────────────────────────────────────────────

    private fun mergeRecords(table: Table, cols: RecordCols, userId: String, incoming: List<EncryptedRecordDto>) {
        for (dto in incoming) {
            val existing = table.selectAll()
                .where { (cols.id eq dto.id) and (cols.userId eq userId) }
                .singleOrNull()?.get(cols.updatedAt)
            if (existing != null && existing >= dto.updatedAt.toJavaInstant()) continue
            table.upsert(cols.id) {
                it[cols.id] = dto.id
                it[cols.userId] = userId
                it[cols.blob] = dto.blob
                it[cols.updatedAt] = dto.updatedAt.toJavaInstant()
                it[cols.deleted] = dto.deleted
            }
        }
    }

    private fun mergeHabitLogs(userId: String, incoming: List<EncryptedLogDto>) {
        for (dto in incoming) {
            HabitLogs.insertIgnore {
                it[id] = dto.id
                it[HabitLogs.userId] = userId
                it[dedupeKey] = dto.dedupeKey
                it[blob] = dto.blob
            }
        }
    }

    // ── pull ────────────────────────────────────────────────────────────────

    private fun pullRecords(table: Table, cols: RecordCols, userId: String, since: JInstant?): List<EncryptedRecordDto> =
        table.selectAll()
            .where { (cols.userId eq userId).let { c -> if (since != null) c and (cols.updatedAt greater since) else c } }
            .map { row ->
                EncryptedRecordDto(
                    id = row[cols.id],
                    updatedAt = row[cols.updatedAt].toKotlinInstant(),
                    deleted = row[cols.deleted],
                    blob = row[cols.blob],
                )
            }

    private fun pullHabitLogs(userId: String): List<EncryptedLogDto> =
        HabitLogs.selectAll().where { HabitLogs.userId eq userId }.map { it.toLogDto() }

    private fun ResultRow.toLogDto() = EncryptedLogDto(
        id = this[HabitLogs.id],
        dedupeKey = this[HabitLogs.dedupeKey],
        blob = this[HabitLogs.blob],
    )
}
