package com.time.applauncher.goalgaurd.cloud

import com.time.applauncher.goalgaurd.core.crypto.BlindIndex
import com.time.applauncher.goalgaurd.core.crypto.EncryptedBlob
import com.time.applauncher.goalgaurd.core.crypto.JsonCryptoCodec
import com.time.applauncher.goalgaurd.core.domain.BackupBundle
import com.time.applauncher.goalgaurd.core.domain.FocusSessionDto
import com.time.applauncher.goalgaurd.core.domain.GoalDto
import com.time.applauncher.goalgaurd.core.domain.HabitDto
import com.time.applauncher.goalgaurd.core.domain.HabitLogDto
import com.time.applauncher.goalgaurd.shared.model.EncryptedLogDto
import com.time.applauncher.goalgaurd.shared.model.EncryptedRecordDto
import com.time.applauncher.goalgaurd.shared.model.SyncPayload
import kotlinx.datetime.toKotlinInstant
import java.time.Instant

// Bridges the local backup DTOs (java.time) and the **encrypted** wire payload. Each record's full
// content is AES-GCM-encrypted into an opaque blob under the vault DEK; only the merge metadata
// (id / updatedAt) travels in clear. Habit logs additionally carry a blind dedupe index so the
// server can union them without learning the habit or date.

fun BackupBundle.toEncryptedSyncPayload(dek: ByteArray, codec: JsonCryptoCodec): SyncPayload {
    val indexKey = BlindIndex.deriveIndexKey(dek)
    return SyncPayload(
        goals = goals.map { it.toEncryptedRecord(dek, codec) },
        habits = habits.map { it.toEncryptedRecord(dek, codec) },
        habitLogs = habitLogs.map { it.toEncryptedLog(dek, codec, indexKey) },
        focusSessions = focusSessions.map { it.toEncryptedRecord(dek, codec) },
    )
}

fun SyncPayload.toBackupBundle(dek: ByteArray, codec: JsonCryptoCodec): BackupBundle = BackupBundle(
    createdAt = Instant.now(),
    goals = goals.map { codec.decrypt(GoalDto.serializer(), EncryptedBlob.decode(it.blob), dek) },
    habits = habits.map { codec.decrypt(HabitDto.serializer(), EncryptedBlob.decode(it.blob), dek) },
    habitLogs = habitLogs.map { codec.decrypt(HabitLogDto.serializer(), EncryptedBlob.decode(it.blob), dek) },
    focusSessions = focusSessions.map { codec.decrypt(FocusSessionDto.serializer(), EncryptedBlob.decode(it.blob), dek) },
)

private fun GoalDto.toEncryptedRecord(dek: ByteArray, codec: JsonCryptoCodec) = EncryptedRecordDto(
    id = id,
    updatedAt = updatedAt.toKotlinInstant(),
    blob = codec.encrypt(GoalDto.serializer(), this, dek).encode(),
)

private fun HabitDto.toEncryptedRecord(dek: ByteArray, codec: JsonCryptoCodec) = EncryptedRecordDto(
    id = id,
    updatedAt = updatedAt.toKotlinInstant(),
    blob = codec.encrypt(HabitDto.serializer(), this, dek).encode(),
)

private fun FocusSessionDto.toEncryptedRecord(dek: ByteArray, codec: JsonCryptoCodec) = EncryptedRecordDto(
    id = id,
    updatedAt = updatedAt.toKotlinInstant(),
    blob = codec.encrypt(FocusSessionDto.serializer(), this, dek).encode(),
)

private fun HabitLogDto.toEncryptedLog(dek: ByteArray, codec: JsonCryptoCodec, indexKey: ByteArray) = EncryptedLogDto(
    id = id,
    dedupeKey = BlindIndex.compute(indexKey, "$habitId|$date"),
    blob = codec.encrypt(HabitLogDto.serializer(), this, dek).encode(),
)
