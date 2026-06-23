package com.time.applauncher.goalgaurd.core.backup

import com.time.applauncher.goalgaurd.core.crypto.KeyEnvelope
import kotlinx.serialization.Serializable

/**
 * On-disk format of an encrypted backup file. Self-contained and portable: it embeds the
 * [envelope] (passphrase-wrapped DEK) so the file can be restored on a fresh install — even by a
 * local-only user with no account — by entering the passphrase. [data] is the AES-GCM ciphertext
 * (`EncryptedBlob.encode`) of the [com.time.applauncher.goalgaurd.core.domain.BackupBundle] JSON
 * under the DEK that [envelope] wraps.
 */
@Serializable
data class EncryptedBackup(
    val version: Int = CURRENT_VERSION,
    val envelope: KeyEnvelope,
    val data: String,
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}
