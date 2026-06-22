package com.time.applauncher.goalgaurd.shared

import com.time.applauncher.goalgaurd.shared.api.GoalGuardJson
import com.time.applauncher.goalgaurd.shared.model.GoalSyncDto
import com.time.applauncher.goalgaurd.shared.model.SyncPayload
import com.time.applauncher.goalgaurd.shared.model.SyncRequest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncDtoSerializationTest {

    @Test
    fun goalSyncDto_roundTrips_throughJson() {
        val goal = GoalSyncDto(
            id = "g1",
            name = "Buy a house",
            emoji = "🏠",
            targetValue = 100.0,
            currentValue = 40.0,
            unit = "%",
            targetDate = LocalDate(2028, 12, 31),
            priority = "HIGH",
            createdAt = LocalDate(2026, 1, 1),
            updatedAt = Instant.parse("2026-06-18T10:00:00Z"),
        )
        val json = GoalGuardJson.encodeToString(goal)
        assertEquals(goal, GoalGuardJson.decodeFromString<GoalSyncDto>(json))
    }

    @Test
    fun syncRequest_roundTrips_withNestedPayload() {
        val request = SyncRequest(
            since = Instant.parse("2026-06-01T00:00:00Z"),
            changes = SyncPayload(
                goals = listOf(
                    GoalSyncDto(
                        id = "g1", name = "Run 5k", emoji = "🏃", targetValue = 5.0, currentValue = 2.0,
                        unit = "km", targetDate = LocalDate(2026, 9, 1), priority = "MEDIUM",
                        createdAt = LocalDate(2026, 6, 1), updatedAt = Instant.parse("2026-06-18T09:00:00Z"),
                    ),
                ),
            ),
        )
        val json = GoalGuardJson.encodeToString(request)
        assertEquals(request, GoalGuardJson.decodeFromString<SyncRequest>(json))
    }
}
