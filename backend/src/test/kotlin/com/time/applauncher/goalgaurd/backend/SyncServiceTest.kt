package com.time.applauncher.goalgaurd.backend

import com.time.applauncher.goalgaurd.backend.config.DbConfig
import com.time.applauncher.goalgaurd.backend.db.DatabaseFactory
import com.time.applauncher.goalgaurd.backend.sync.SyncService
import com.time.applauncher.goalgaurd.shared.model.GoalSyncDto
import com.time.applauncher.goalgaurd.shared.model.HabitLogSyncDto
import com.time.applauncher.goalgaurd.shared.model.SyncPayload
import com.time.applauncher.goalgaurd.shared.model.SyncRequest
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SyncServiceTest {

    private val service = SyncService()
    private val user = "u-test"

    @BeforeAll
    fun setup() {
        DatabaseFactory.init(
            DbConfig("jdbc:h2:mem:synctest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL", "org.h2.Driver", "sa", ""),
        )
    }

    private fun goal(value: Double, updatedAt: String) = GoalSyncDto(
        id = "g1", name = "Goal", emoji = "🎯", targetValue = 100.0, currentValue = value, unit = "%",
        targetDate = LocalDate(2028, 1, 1), priority = "HIGH", createdAt = LocalDate(2026, 1, 1),
        updatedAt = Instant.parse(updatedAt),
    )

    @Test
    fun lastWriteWins_keepsNewerRow_andRejectsOlder() = runTest {
        // Newer row first.
        service.sync(user, SyncRequest(changes = SyncPayload(goals = listOf(goal(40.0, "2026-06-10T00:00:00Z")))))
        // Older update for the same id must NOT overwrite.
        var res = service.sync(user, SyncRequest(changes = SyncPayload(goals = listOf(goal(99.0, "2026-06-01T00:00:00Z")))))
        assertEquals(40.0, res.changes.goals.single { it.id == "g1" }.currentValue)
        // Newer update wins.
        res = service.sync(user, SyncRequest(changes = SyncPayload(goals = listOf(goal(75.0, "2026-06-20T00:00:00Z")))))
        assertEquals(75.0, res.changes.goals.single { it.id == "g1" }.currentValue)
    }

    @Test
    fun habitLogs_mergeByUnion_withoutDuplicates() = runTest {
        val log = HabitLogSyncDto(id = "l1", habitId = "h1", date = LocalDate(2026, 6, 18), isCompleted = true)
        service.sync("u-logs", SyncRequest(changes = SyncPayload(habitLogs = listOf(log))))
        // Re-pushing the same log is ignored (insert-or-ignore on the unique key).
        val res = service.sync("u-logs", SyncRequest(changes = SyncPayload(habitLogs = listOf(log))))
        assertEquals(1, res.changes.habitLogs.count { it.habitId == "h1" })
    }
}
