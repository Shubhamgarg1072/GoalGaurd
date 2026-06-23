package com.time.applauncher.goalgaurd.feature.vault.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.time.applauncher.goalgaurd.core.designsystem.theme.Primary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextMuted
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextPrimary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextSecondary
import com.time.applauncher.goalgaurd.core.presentation.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun VaultSetupRoot(
    onComplete: () -> Unit,
    viewModel: VaultViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { if (it is VaultEvent.Unlocked) onComplete() }
    VaultSetupScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun VaultUnlockRoot(
    onUnlocked: () -> Unit,
    viewModel: VaultViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { if (it is VaultEvent.Unlocked) onUnlocked() }
    VaultUnlockScreen(state = state, onAction = viewModel::onAction)
}

@Composable
private fun VaultSetupScreen(
    state: VaultUiState,
    onAction: (VaultAction) -> Unit,
) {
    var passphrase by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    VaultScaffold(
        title = "Protect your data",
        subtitle = "Create a passphrase to encrypt everything on this device and in any cloud backup. " +
            "Only this passphrase can unlock it — there is no recovery, so keep it safe.",
    ) {
        PassphraseField(
            value = passphrase,
            onValueChange = { passphrase = it; onAction(VaultAction.DismissError) },
            label = "Passphrase",
        )
        Spacer(Modifier.height(12.dp))
        PassphraseField(
            value = confirm,
            onValueChange = { confirm = it; onAction(VaultAction.DismissError) },
            label = "Confirm passphrase",
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "Set up encryption",
            isLoading = state.isLoading,
            enabled = passphrase.isNotEmpty() && confirm.isNotEmpty(),
            onClick = { onAction(VaultAction.SubmitSetUp(passphrase, confirm)) },
        )
        ErrorText(state.error)
    }
}

@Composable
private fun VaultUnlockScreen(
    state: VaultUiState,
    onAction: (VaultAction) -> Unit,
) {
    var passphrase by rememberSaveable { mutableStateOf("") }

    VaultScaffold(
        title = "Unlock GoalGuard",
        subtitle = "Enter your passphrase to decrypt your goals, habits and focus data.",
    ) {
        PassphraseField(
            value = passphrase,
            onValueChange = { passphrase = it; onAction(VaultAction.DismissError) },
            label = "Passphrase",
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "Unlock",
            isLoading = state.isLoading,
            enabled = passphrase.isNotEmpty(),
            onClick = { onAction(VaultAction.SubmitUnlock(passphrase)) },
        )
        ErrorText(state.error)
    }
}

@Composable
private fun VaultScaffold(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(subtitle, color = TextSecondary, fontSize = 15.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        content()
    }
}

@Composable
private fun PassphraseField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = Primary,
            unfocusedBorderColor = TextMuted,
            focusedLabelColor = Primary,
            unfocusedLabelColor = TextMuted,
            cursorColor = Primary,
        ),
    )
}

@Composable
private fun PrimaryButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White),
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.height(20.dp))
        } else {
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ErrorText(error: String?) {
    error?.let {
        Spacer(Modifier.height(16.dp))
        Text(it, color = Color(0xFFFF6B6B), fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}
