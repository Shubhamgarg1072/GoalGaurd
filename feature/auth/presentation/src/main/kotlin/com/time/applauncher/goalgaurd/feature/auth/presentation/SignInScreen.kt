package com.time.applauncher.goalgaurd.feature.auth.presentation

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.time.applauncher.goalgaurd.core.designsystem.theme.BackgroundDeep
import com.time.applauncher.goalgaurd.core.designsystem.theme.Primary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextMuted
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextPrimary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextSecondary
import com.time.applauncher.goalgaurd.core.domain.Result
import com.time.applauncher.goalgaurd.core.presentation.ObserveAsEvents
import com.time.applauncher.goalgaurd.feature.auth.data.GoogleCredentialProvider
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun SignInRoot(
    onContinue: () -> Unit,
    viewModel: SignInViewModel = koinViewModel(),
    credentialProvider: GoogleCredentialProvider = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            SignInEvent.Continue -> onContinue()
            SignInEvent.RequestGoogleCredential -> scope.launch {
                when (val result = credentialProvider.getIdToken(context)) {
                    is Result.Success -> viewModel.onAction(SignInAction.TokenReceived(result.data))
                    is Result.Error -> viewModel.onAction(SignInAction.CredentialFailed)
                }
            }
        }
    }

    SignInScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun SignInScreen(
    state: SignInState,
    onAction: (SignInAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Sync your progress",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Sign in with Google to back up your goals, habits and focus sessions across devices. " +
                "This is optional — GoalGuard works fully offline without an account.",
            color = TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(36.dp))

        Button(
            onClick = { onAction(SignInAction.GoogleSignInClicked) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.height(20.dp))
            } else {
                Text("Continue with Google", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { onAction(SignInAction.SkipClicked) }, enabled = !state.isLoading) {
            Text("Skip for now", color = TextMuted)
        }

        state.error?.let { error ->
            Spacer(Modifier.height(16.dp))
            Text(error, color = Color(0xFFFF6B6B), fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}
