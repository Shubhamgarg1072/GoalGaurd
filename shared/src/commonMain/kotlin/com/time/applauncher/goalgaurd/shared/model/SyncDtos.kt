package com.time.applauncher.goalgaurd.shared.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

// End-to-end-encrypted wire DTOs. The server stores and merges these without ever seeing the
// plaintext content: only the merge metadata (id / updatedAt / deleted, or a blind dedupe index)
// is in clear. [blob] is `EncryptedBlob.encode` — AES-GCM ciphertext under the user's data key.

/**
 * An encrypted goal / habit / focus-session record. Last-write-wins merges on [updatedAt]; a
 * [deleted] tombstone propagates deletions. [blob] is the opaque encrypted content.
 */
@Serializable
data class EncryptedRecordDto(
    val id: String,
    val updatedAt: Instant,
    val deleted: Boolean = false,
    val blob: String,
)

/**
 * An encrypted habit log (append-only). [dedupeKey] is a blind index — `HMAC(indexKey, habitId|date)`
 * — letting the server enforce the per-user union without learning the habit or date. [blob] is the
 * opaque encrypted content.
 */
@Serializable
data class EncryptedLogDto(
    val id: String,
    val dedupeKey: String,
    val blob: String,
)

@Serializable
data class SyncPayload(
    val goals: List<EncryptedRecordDto> = emptyList(),
    val habits: List<EncryptedRecordDto> = emptyList(),
    val habitLogs: List<EncryptedLogDto> = emptyList(),
    val focusSessions: List<EncryptedRecordDto> = emptyList(),
)

/** [since] = the client's last successful sync time; null = first sync (full push). */
@Serializable
data class SyncRequest(
    val since: Instant? = null,
    val changes: SyncPayload = SyncPayload(),
)

/** [changes] = rows the server knows that are newer than [SyncRequest.since]. */
@Serializable
data class SyncResponse(
    val serverTime: Instant,
    val changes: SyncPayload = SyncPayload(),
)
