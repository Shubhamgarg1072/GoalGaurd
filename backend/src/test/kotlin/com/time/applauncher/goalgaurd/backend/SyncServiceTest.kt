package com.time.applauncher.goalgaurd.backend

import com.time.applauncher.goalgaurd.backend.config.DbConfig
import com.time.applauncher.goalgaurd.backend.db.DatabaseFactory
import com.time.applauncher.goalgaurd.backend.sync.SyncService
import com.time.applauncher.goalgaurd.shared.model.EncryptedLogDto
import com.time.applauncher.goalgaurd.shared.model.EncryptedRecordDto
import com.time.applauncher.goalgaurd.shared.model.SyncPayload
import com.time.applauncher.goalgaurd.shared.model.SyncRequest
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
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

    // The server merges on metadata only; the blob is an opaque ciphertext placeholder here.
    private fun goal(blob: String, updatedAt: String) =
        EncryptedRecordDto(id = "g1", updatedAt = Instant.parse(updatedAt), blob = blob)

    @Test
    fun lastWriteWins_keepsNewerRow_andRejectsOlder() = runTest {
        // Newer row first.
        service.sync(user, SyncRequest(changes = SyncPayload(goals = listOf(goal("v40", "2026-06-10T00:00:00Z")))))
        // Older update for the same id must NOT overwrite.
        var res = service.sync(user, SyncRequest(changes = SyncPayload(goals = listOf(goal("v99", "2026-06-01T00:00:00Z")))))
        assertEquals("v40", res.changes.goals.single { it.id == "g1" }.blob)
        // Newer update wins.
        res = service.sync(user, SyncRequest(changes = SyncPayload(goals = listOf(goal("v75", "2026-06-20T00:00:00Z")))))
        assertEquals("v75", res.changes.goals.single { it.id == "g1" }.blob)
    }

    @Test
    fun habitLogs_mergeByUnion_withoutDuplicates() = runTest {
        val log = EncryptedLogDto(id = "l1", dedupeKey = "blind-index-h1-0618", blob = "enc")
        service.sync("u-logs", SyncRequest(changes = SyncPayload(habitLogs = listOf(log))))
        // Re-pushing the same log is ignored (insert-or-ignore on the unique user+dedupeKey index).
        val res = service.sync("u-logs", SyncRequest(changes = SyncPayload(habitLogs = listOf(log))))
        assertEquals(1, res.changes.habitLogs.count { it.dedupeKey == "blind-index-h1-0618" })
    }
}
