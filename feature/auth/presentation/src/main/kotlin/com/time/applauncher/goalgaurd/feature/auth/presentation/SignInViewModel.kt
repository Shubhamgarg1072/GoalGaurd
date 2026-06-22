package com.time.applauncher.goalgaurd.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.time.applauncher.goalgaurd.core.domain.Result
import com.time.applauncher.goalgaurd.feature.auth.domain.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignInViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    private val _events = Channel<SignInEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: SignInAction) {
        when (action) {
            SignInAction.GoogleSignInClicked -> {
                _state.update { it.copy(isLoading = true, error = null) }
                viewModelScope.launch { _events.send(SignInEvent.RequestGoogleCredential) }
            }
            is SignInAction.TokenReceived -> signIn(action.idToken)
            SignInAction.CredentialFailed -> _state.update {
                it.copy(isLoading = false, error = "Google sign-in was cancelled or failed.")
            }
            SignInAction.SkipClicked -> viewModelScope.launch { _events.send(SignInEvent.Continue) }
            SignInAction.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun signIn(idToken: String) {
        viewModelScope.launch {
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is Result.Success -> _events.send(SignInEvent.Continue)
                is Result.Error -> _state.update {
                    it.copy(isLoading = false, error = "Couldn't sign in. You can keep using the app offline.")
                }
            }
        }
    }
}

data class SignInState(
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface SignInAction {
    data object GoogleSignInClicked : SignInAction
    data class TokenReceived(val idToken: String) : SignInAction
    data object CredentialFailed : SignInAction
    data object SkipClicked : SignInAction
    data object DismissError : SignInAction
}

sealed interface SignInEvent {
    /** Ask the UI layer to launch the Credential Manager flow. */
    data object RequestGoogleCredential : SignInEvent
    /** Proceed into the app (signed in or skipped). */
    data object Continue : SignInEvent
}
