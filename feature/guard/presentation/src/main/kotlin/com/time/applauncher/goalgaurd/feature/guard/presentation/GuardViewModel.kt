package com.time.applauncher.goalgaurd.feature.guard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.time.applauncher.goalgaurd.feature.guard.domain.GuardConfigRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GuardViewModel(
    private val configRepository: GuardConfigRepository,
) : ViewModel() {

    private val _events = Channel<GuardEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val state = configRepository.observeConfig()
        .map { config ->
            GuardState(
                isLoading = false,
                enabled = config.enabled,
                continuousMinutesThreshold = config.continuousMinutesThreshold,
                monitoredAppCount = config.monitoredPackages.size,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GuardState())

    fun onAction(action: GuardAction) {
        when (action) {
            is GuardAction.SetEnabled -> viewModelScope.launch {
                configRepository.setEnabled(action.enabled)
                _events.send(if (action.enabled) GuardEvent.StartService else GuardEvent.StopService)
            }

            is GuardAction.SetThreshold -> viewModelScope.launch {
                configRepository.setContinuousMinutesThreshold(action.minutes.coerceIn(MIN_THRESHOLD, MAX_THRESHOLD))
            }
        }
    }

    companion object {
        const val MIN_THRESHOLD = 5
        const val MAX_THRESHOLD = 60
    }
}

data class GuardState(
    val isLoading: Boolean = true,
    val enabled: Boolean = false,
    val continuousMinutesThreshold: Int = 15,
    val monitoredAppCount: Int = 0,
)

sealed interface GuardAction {
    data class SetEnabled(val enabled: Boolean) : GuardAction
    data class SetThreshold(val minutes: Int) : GuardAction
}

sealed interface GuardEvent {
    data object StartService : GuardEvent
    data object StopService : GuardEvent
}
