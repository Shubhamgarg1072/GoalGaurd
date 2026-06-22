package com.time.applauncher.goalgaurd.feature.focus.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.time.applauncher.goalgaurd.feature.focus.domain.FocusRepository
import com.time.applauncher.goalgaurd.feature.focus.domain.FocusSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

class FocusViewModel(private val repository: FocusRepository) : ViewModel() {

    private val _state = MutableStateFlow(FocusState())
    val state = _state
        .onStart { observeTodayMinutes() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FocusState())

    private val _events = Channel<FocusEvent>()
    val events = _events.receiveAsFlow()

    private var timerJob: Job? = null
    private var sessionStartTime: LocalDateTime? = null

    fun onAction(action: FocusAction) {
        when (action) {
            is FocusAction.OnDurationSelect -> _state.update { it.copy(selectedDurationMinutes = action.minutes) }
            FocusAction.OnStartSession -> startSession()
            FocusAction.OnEndSession -> endSession(completed = true)
            FocusAction.OnAbandonSession -> endSession(completed = false)
            FocusAction.OnDismissSessionDone -> {
                _state.update { it.copy(isSessionDone = false) }
                viewModelScope.launch { _events.send(FocusEvent.NavigateBack) }
            }
        }
    }

    private fun observeTodayMinutes() {
        viewModelScope.launch {
            repository.observeTodayFocusMinutes().collect { minutes ->
                _state.update { it.copy(todayFocusMinutes = minutes) }
            }
        }
    }

    private fun startSession() {
        val duration = _state.value.selectedDurationMinutes
        sessionStartTime = LocalDateTime.now()
        val totalSeconds = duration * 60
        _state.update {
            it.copy(
                isSessionActive = true,
                isSessionDone = false,
                remainingSeconds = totalSeconds,
                totalSeconds = totalSeconds,
            )
        }
        timerJob = viewModelScope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1_000)
                remaining--
                _state.update { it.copy(remainingSeconds = remaining) }
            }
            endSession(completed = true)
        }
    }

    private fun endSession(completed: Boolean) {
        timerJob?.cancel()
        val startedAt = sessionStartTime ?: LocalDateTime.now()
        val duration = _state.value.selectedDurationMinutes
        viewModelScope.launch {
            repository.saveSession(
                FocusSession(
                    id = UUID.randomUUID().toString(),
                    durationMinutes = duration,
                    startedAt = startedAt,
                    completedAt = LocalDateTime.now(),
                    isCompleted = completed,
                )
            )
            if (completed) {
                _state.update { it.copy(isSessionActive = false, remainingSeconds = 0, isSessionDone = true) }
            } else {
                _state.update { it.copy(isSessionActive = false, remainingSeconds = 0, isSessionDone = false) }
                _events.send(FocusEvent.NavigateBack)
            }
        }
    }
}

data class FocusState(
    val selectedDurationMinutes: Int = 30,
    val isSessionActive: Boolean = false,
    val isSessionDone: Boolean = false,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val todayFocusMinutes: Int = 0,
) {
    val progressFraction: Float
        get() = if (totalSeconds == 0) 0f else 1f - remainingSeconds.toFloat() / totalSeconds

    val formattedRemaining: String
        get() {
            val m = remainingSeconds / 60
            val s = remainingSeconds % 60
            return "%02d:%02d".format(m, s)
        }
}

sealed interface FocusAction {
    data class OnDurationSelect(val minutes: Int) : FocusAction
    data object OnStartSession : FocusAction
    data object OnEndSession : FocusAction
    data object OnAbandonSession : FocusAction
    data object OnDismissSessionDone : FocusAction
}

sealed interface FocusEvent {
    data object NavigateBack : FocusEvent
}
