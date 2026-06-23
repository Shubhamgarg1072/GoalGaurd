package com.time.applauncher.goalgaurd.feature.backup.presentation

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.time.applauncher.goalgaurd.core.domain.BackupBundle
import com.time.applauncher.goalgaurd.core.domain.BackupRepository
import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.Result
import com.time.applauncher.goalgaurd.core.domain.onFailure
import com.time.applauncher.goalgaurd.core.domain.onSuccess
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

private val Context.backupDataStore: DataStore<Preferences> by preferencesDataStore("backup")
private val LAST_EXPORT_KEY = longPreferencesKey("last_export_epoch_ms")

class BackupViewModel(
    private val backupRepository: BackupRepository,
    private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(BackupState())
    val state = _state
        .onStart { observeLastExport() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BackupState())

    private val _events = Channel<BackupEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: BackupAction) {
        when (action) {
            BackupAction.OnExportClick -> viewModelScope.launch {
                _events.send(BackupEvent.LaunchExportPicker)
            }
            BackupAction.OnImportClick -> viewModelScope.launch {
                _events.send(BackupEvent.LaunchImportPicker)
            }
            is BackupAction.OnExportUriReceived -> exportToUri(action.uri)
            is BackupAction.OnImportUriReceived -> importFromUri(action.uri)
            is BackupAction.OnImportPassphraseSubmit -> submitImportPassphrase(action.passphrase)
            BackupAction.OnImportPassphraseDismiss ->
                _state.update { it.copy(pendingImportJson = null, passphraseError = null, isImporting = false) }
        }
    }

    private fun exportToUri(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, message = null) }
            backupRepository.export()
                .onSuccess { bundle ->
                    when (val encoded = backupRepository.toJson(bundle)) {
                        is Result.Success -> try {
                            context.contentResolver.openOutputStream(uri)?.use { out ->
                                out.write(encoded.data.toByteArray(Charsets.UTF_8))
                            }
                            val nowMs = Instant.now().toEpochMilli()
                            context.backupDataStore.edit { it[LAST_EXPORT_KEY] = nowMs }
                            _state.update { it.copy(isExporting = false, lastExportMs = nowMs, message = "Backup exported (encrypted)") }
                        } catch (e: Exception) {
                            _state.update { it.copy(isExporting = false, message = "Export failed: ${e.message}") }
                        }
                        is Result.Error -> _state.update {
                            it.copy(isExporting = false, message = "Unlock the vault before exporting")
                        }
                    }
                }
                .onFailure {
                    _state.update { it.copy(isExporting = false, message = "Export failed") }
                }
        }
    }

    private fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isImporting = true, message = null) }
            val jsonStr = try {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
            } catch (e: Exception) {
                null
            }
            if (jsonStr == null) {
                _state.update { it.copy(isImporting = false, message = "Could not read file") }
                return@launch
            }
            decodeAndApply(jsonStr, passphrase = null)
        }
    }

    private fun submitImportPassphrase(passphrase: String) {
        val jsonStr = _state.value.pendingImportJson ?: return
        viewModelScope.launch {
            _state.update { it.copy(passphraseError = null) }
            decodeAndApply(jsonStr, passphrase = passphrase)
        }
    }

    /** Decrypts (optionally with [passphrase]) then restores. Prompts for a passphrase if the file needs one. */
    private suspend fun decodeAndApply(jsonStr: String, passphrase: String?) {
        when (val decoded = backupRepository.fromJson(jsonStr, passphrase)) {
            is Result.Success -> applyBundle(decoded.data)
            is Result.Error -> when (decoded.error) {
                DataError.Local.ENCRYPTED_NEEDS_PASSPHRASE ->
                    _state.update { it.copy(isImporting = true, pendingImportJson = jsonStr, passphraseError = null) }
                DataError.Local.WRONG_PASSPHRASE ->
                    _state.update { it.copy(passphraseError = "Incorrect passphrase.") }
                else ->
                    _state.update { it.copy(isImporting = false, pendingImportJson = null, message = "Invalid backup file") }
            }
        }
    }

    private suspend fun applyBundle(bundle: BackupBundle) {
        backupRepository.importBundle(bundle)
            .onSuccess {
                _state.update { it.copy(isImporting = false, pendingImportJson = null, passphraseError = null, message = "Restore complete") }
            }
            .onFailure {
                _state.update { it.copy(isImporting = false, pendingImportJson = null, message = "Restore failed") }
            }
    }

    private fun observeLastExport() {
        viewModelScope.launch {
            context.backupDataStore.data
                .map { it[LAST_EXPORT_KEY] }
                .collect { ms ->
                    _state.update { it.copy(lastExportMs = ms) }
                }
        }
    }
}

data class BackupState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val lastExportMs: Long? = null,
    val message: String? = null,
    /** Non-null while we await the passphrase for an encrypted backup made by a different vault. */
    val pendingImportJson: String? = null,
    val passphraseError: String? = null,
) {
    val isAwaitingPassphrase: Boolean get() = pendingImportJson != null
}

sealed interface BackupAction {
    data object OnExportClick : BackupAction
    data object OnImportClick : BackupAction
    data class OnExportUriReceived(val uri: Uri) : BackupAction
    data class OnImportUriReceived(val uri: Uri) : BackupAction
    data class OnImportPassphraseSubmit(val passphrase: String) : BackupAction
    data object OnImportPassphraseDismiss : BackupAction
}

sealed interface BackupEvent {
    data object LaunchExportPicker : BackupEvent
    data object LaunchImportPicker : BackupEvent
}
