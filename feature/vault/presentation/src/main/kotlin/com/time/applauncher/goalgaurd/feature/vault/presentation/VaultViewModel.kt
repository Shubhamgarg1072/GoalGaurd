package com.time.applauncher.goalgaurd.feature.vault.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.Result
import com.time.applauncher.goalgaurd.feature.vault.domain.VaultRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MIN_PASSPHRASE_LENGTH = 8

class VaultViewModel(
    private val vaultRepository: VaultRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(VaultUiState())
    val state = _state.asStateFlow()

    private val _events = Channel<VaultEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: VaultAction) {
        when (action) {
            is VaultAction.SubmitSetUp -> setUp(action.passphrase, action.confirmPassphrase)
            is VaultAction.SubmitUnlock -> unlock(action.passphrase)
            VaultAction.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun setUp(passphrase: String, confirm: String) {
        when {
            passphrase.length < MIN_PASSPHRASE_LENGTH ->
                return fail("Use at least $MIN_PASSPHRASE_LENGTH characters.")
            passphrase != confirm ->
                return fail("The passphrases don't match.")
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (vaultRepository.setUp(passphrase)) {
                is Result.Success -> _events.send(VaultEvent.Unlocked)
                is Result.Error -> fail("Couldn't set up encryption. Please try again.")
            }
        }
    }

    private fun unlock(passphrase: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = vaultRepository.unlock(passphrase)) {
                is Result.Success -> _events.send(VaultEvent.Unlocked)
                is Result.Error -> fail(
                    if (result.error == DataError.Local.WRONG_PASSPHRASE) "Incorrect passphrase."
                    else "Couldn't unlock. Please try again.",
                )
            }
        }
    }

    private fun fail(message: String) {
        _state.update { it.copy(isLoading = false, error = message) }
    }
}

data class VaultUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface VaultAction {
    data class SubmitSetUp(val passphrase: String, val confirmPassphrase: String) : VaultAction
    data class SubmitUnlock(val passphrase: String) : VaultAction
    data object DismissError : VaultAction
}

sealed interface VaultEvent {
    /** The vault is now unlocked (after set-up or unlock); proceed into the app. */
    data object Unlocked : VaultEvent
}
