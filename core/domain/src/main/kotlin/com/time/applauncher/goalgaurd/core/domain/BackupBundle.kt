package com.time.applauncher.goalgaurd.core.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

// ── Custom serializers for java.time types ────────────────────────────────────

object InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}

object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
}

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDateTime) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): LocalDateTime = LocalDateTime.parse(decoder.decodeString())
}

// ── Backup DTOs ───────────────────────────────────────────────────────────────

@Serializable
data class GoalDto(
    val id: String,
    val name: String,
    val emoji: String,
    val targetValue: Double,
    val currentValue: Double,
    val unit: String,
    @Serializable(with = LocalDateSerializer::class) val targetDate: LocalDate,
    val priority: String,
    @Serializable(with = LocalDateSerializer::class) val createdAt: LocalDate,
    @Serializable(with = InstantSerializer::class) val updatedAt: Instant,
)

@Serializable
data class HabitDto(
    val id: String,
    val goalId: String?,
    val name: String,
    val emoji: String,
    val frequency: String,
    val difficulty: String,
    val reminderTime: String?,
    val streak: Int,
    val isActive: Boolean,
    @Serializable(with = InstantSerializer::class) val updatedAt: Instant,
)

@Serializable
data class HabitLogDto(
    val id: String,
    val habitId: String,
    @Serializable(with = LocalDateSerializer::class) val date: LocalDate,
    val isCompleted: Boolean,
)

@Serializable
data class FocusSessionDto(
    val id: String,
    val durationMinutes: Int,
    @Serializable(with = LocalDateTimeSerializer::class) val startedAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) val completedAt: LocalDateTime?,
    val isCompleted: Boolean,
    @Serializable(with = InstantSerializer::class) val updatedAt: Instant,
)

@Serializable
data class BackupBundle(
    val version: Int = 1,
    @Serializable(with = InstantSerializer::class) val createdAt: Instant,
    val goals: List<GoalDto>,
    val habits: List<HabitDto>,
    val habitLogs: List<HabitLogDto>,
    val focusSessions: List<FocusSessionDto>,
)

// ── Repository interface ──────────────────────────────────────────────────────

interface BackupRepository {
    suspend fun export(): Result<BackupBundle, DataError>
    suspend fun importBundle(bundle: BackupBundle): EmptyResult<DataError>
    suspend fun toJson(bundle: BackupBundle): String
    suspend fun fromJson(json: String): Result<BackupBundle, DataError>
}
