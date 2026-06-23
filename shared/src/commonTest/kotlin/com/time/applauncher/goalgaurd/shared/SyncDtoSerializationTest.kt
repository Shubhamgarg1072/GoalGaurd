package com.time.applauncher.goalgaurd.shared

import com.time.applauncher.goalgaurd.shared.api.GoalGuardJson
import com.time.applauncher.goalgaurd.shared.model.EncryptedLogDto
import com.time.applauncher.goalgaurd.shared.model.EncryptedRecordDto
import com.time.applauncher.goalgaurd.shared.model.SyncPayload
import com.time.applauncher.goalgaurd.shared.model.SyncRequest
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncDtoSerializationTest {

    @Test
    fun encryptedRecordDto_roundTrips_throughJson() {
        val record = EncryptedRecordDto(
            id = "g1",
            updatedAt = Instant.parse("2026-06-18T10:00:00Z"),
            deleted = false,
            blob = "bm9uY2U.Y2lwaGVydGV4dA",
        )
        val json = GoalGuardJson.encodeToString(record)
        assertEquals(record, GoalGuardJson.decodeFromString<EncryptedRecordDto>(json))
    }

    @Test
    fun syncRequest_roundTrips_withEncryptedPayload() {
        val request = SyncRequest(
            since = Instant.parse("2026-06-01T00:00:00Z"),
            changes = SyncPayload(
                goals = listOf(
                    EncryptedRecordDto("g1", Instant.parse("2026-06-18T09:00:00Z"), false, "YQ.Yg"),
                ),
                habitLogs = listOf(
                    EncryptedLogDto(id = "l1", dedupeKey = "ZGstaDEtMDYxOA", blob = "Yw.ZA"),
                ),
            ),
        )
        val json = GoalGuardJson.encodeToString(request)
        assertEquals(request, GoalGuardJson.decodeFromString<SyncRequest>(json))
    }
}
