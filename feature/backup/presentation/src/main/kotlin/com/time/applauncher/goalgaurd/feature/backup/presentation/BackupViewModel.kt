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
import com.time.applauncher.goalgaurd.core.domain.BackupRepository
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
        }
    }

    private fun exportToUri(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, message = null) }
            backupRepository.export()
                .onSuccess { bundle ->
                    val jsonStr = backupRepository.toJson(bundle)
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(jsonStr.toByteArray(Charsets.UTF_8))
                        }
                        val nowMs = Instant.now().toEpochMilli()
                        context.backupDataStore.edit { it[LAST_EXPORT_KEY] = nowMs }
                        _state.update { it.copy(isExporting = false, lastExportMs = nowMs, message = "Backup exported successfully") }
                    } catch (e: Exception) {
                        _state.update { it.copy(isExporting = false, message = "Export failed: ${e.message}") }
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
            try {
                val jsonStr = context.contentResolver.openInputStream(uri)
                    ?.use { it.readBytes().toString(Charsets.UTF_8) }
                    ?: run {
                        _state.update { it.copy(isImporting = false, message = "Could not read file") }
                        return@launch
                    }
                backupRepository.fromJson(jsonStr)
                    .onSuccess { bundle ->
                        backupRepository.importBundle(bundle)
                            .onSuccess {
                                _state.update { it.copy(isImporting = false, message = "Restore complete") }
                            }
                            .onFailure {
                                _state.update { it.copy(isImporting = false, message = "Restore failed") }
                            }
                    }
                    .onFailure {
                        _state.update { it.copy(isImporting = false, message = "Invalid backup file") }
                    }
            } catch (e: Exception) {
                _state.update { it.copy(isImporting = false, message = "Restore failed: ${e.message}") }
            }
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
)

sealed interface BackupAction {
    data object OnExportClick : BackupAction
    data object OnImportClick : BackupAction
    data class OnExportUriReceived(val uri: Uri) : BackupAction
    data class OnImportUriReceived(val uri: Uri) : BackupAction
}

sealed interface BackupEvent {
    data object LaunchExportPicker : BackupEvent
    data object LaunchImportPicker : BackupEvent
}
