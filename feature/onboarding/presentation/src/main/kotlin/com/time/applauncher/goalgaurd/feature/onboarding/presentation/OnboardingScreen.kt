package com.time.applauncher.goalgaurd.feature.onboarding.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.time.applauncher.goalgaurd.core.designsystem.components.GoalGuardPrimaryButton
import com.time.applauncher.goalgaurd.core.designsystem.theme.BackgroundDeep
import com.time.applauncher.goalgaurd.core.designsystem.theme.BorderSubtle
import com.time.applauncher.goalgaurd.core.designsystem.theme.GoalGuardTheme
import com.time.applauncher.goalgaurd.core.designsystem.theme.Primary
import com.time.applauncher.goalgaurd.core.designsystem.theme.SurfaceVariant
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextMuted
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextPrimary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextSecondary
import com.time.applauncher.goalgaurd.core.presentation.ObserveAsEvents
import com.time.applauncher.goalgaurd.feature.goals.domain.GoalPriority
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingRoot(
    onNavigateToPermissions: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            OnboardingEvent.NavigateToDashboard -> onNavigateToPermissions()
        }
    }
    OnboardingScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun OnboardingScreen(state: OnboardingState, onAction: (OnboardingAction) -> Unit) {
    AnimatedContent(targetState = state.step, label = "onboarding_step") { step ->
        when (step) {
            OnboardingStep.WELCOME -> WelcomeScreen(onAction = onAction)
            OnboardingStep.PROFILE -> ProfileSetupScreen(state = state, onAction = onAction)
            OnboardingStep.CREATE_GOAL -> CreateGoalScreen(state = state, onAction = onAction)
        }
    }
}

@Composable
private fun WelcomeScreen(onAction: (OnboardingAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(Modifier.height(60.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Logo
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Primary),
                contentAlignment = Alignment.Center,
            ) {
                Text("🛡️", fontSize = 40.sp)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "GoalGuard",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = (-1).sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Connect daily habits to your\nbiggest life goals",
                fontSize = 15.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureRow("🎯", "Goal-Driven Life", "Progress toward your biggest dreams")
            FeatureRow("⚡", "Habit Engine", "Build habits that compound over time")
            FeatureRow("🛡️", "Doom Scroll Shield", "Stop scrolling, start achieving")
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GoalGuardPrimaryButton(text = "Get Started →", onClick = { onAction(OnboardingAction.OnGetStarted) })
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = {}) {
                Text("Already have an account? Sign in", fontSize = 13.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun FeatureRow(emoji: String, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceVariant)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun ProfileSetupScreen(state: OnboardingState, onAction: (OnboardingAction) -> Unit) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Primary,
        unfocusedBorderColor = BorderSubtle,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = Primary,
        focusedContainerColor = BackgroundDeep,
        unfocusedContainerColor = BackgroundDeep,
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onAction(OnboardingAction.OnBackToWelcome) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            StepIndicator(current = 1, total = 3)
        }
        Column(modifier = Modifier.padding(horizontal = 28.dp)) {
            Text("Step 1 of 3", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary, letterSpacing = 1.sp)
            Spacer(Modifier.height(6.dp))
            Text("Tell us about you", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text("Personalizes your coaching experience", fontSize = 14.sp, color = TextSecondary)
            Spacer(Modifier.height(28.dp))
            Text("FULL NAME", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.name,
                onValueChange = { onAction(OnboardingAction.OnNameChange(it)) },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                placeholder = { Text("Arjun Sharma", color = TextMuted) },
            )
            Spacer(Modifier.height(16.dp))
            Text("AGE", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.age,
                onValueChange = { onAction(OnboardingAction.OnAgeChange(it)) },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("28", color = TextMuted) },
            )
            Spacer(Modifier.height(16.dp))
            Row {
                Text("OCCUPATION", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Text(" — optional", fontSize = 11.sp, color = TextMuted)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.occupation,
                onValueChange = { onAction(OnboardingAction.OnOccupationChange(it)) },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                placeholder = { Text("Software Engineer", color = TextMuted) },
            )
            Spacer(Modifier.height(32.dp))
            GoalGuardPrimaryButton(
                text = "Continue →",
                onClick = { onAction(OnboardingAction.OnContinueToGoal) },
                enabled = state.name.isNotBlank(),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CreateGoalScreen(state: OnboardingState, onAction: (OnboardingAction) -> Unit) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Primary,
        unfocusedBorderColor = BorderSubtle,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = Primary,
        focusedContainerColor = BackgroundDeep,
        unfocusedContainerColor = BackgroundDeep,
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onAction(OnboardingAction.OnBackToProfile) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            StepIndicator(current = 2, total = 3)
        }
        Column(modifier = Modifier.padding(horizontal = 28.dp)) {
            Text("Step 2 of 3", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary, letterSpacing = 1.sp)
            Spacer(Modifier.height(6.dp))
            Text("Your biggest goal", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text("We'll build habits around it", fontSize = 14.sp, color = TextSecondary)
            Spacer(Modifier.height(28.dp))
            Text("GOAL NAME", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.goalName,
                onValueChange = { onAction(OnboardingAction.OnGoalNameChange(it)) },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                placeholder = { Text("Buy a House", color = TextMuted) },
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("TARGET", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.goalTarget,
                        onValueChange = { onAction(OnboardingAction.OnGoalTargetChange(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors,
                        shape = RoundedCornerShape(14.dp),
                        placeholder = { Text("₹15,00,000", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("CURRENT", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.goalCurrent,
                        onValueChange = { onAction(OnboardingAction.OnGoalCurrentChange(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors,
                        shape = RoundedCornerShape(14.dp),
                        placeholder = { Text("₹5,40,000", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("PRIORITY", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GoalPriority.entries.forEach { priority ->
                    val selected = state.goalPriority == priority
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) Primary.copy(alpha = 0.2f) else SurfaceVariant)
                            .clickable { onAction(OnboardingAction.OnGoalPriorityChange(priority)) }
                            .padding(11.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            priority.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) Primary else TextMuted,
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            GoalGuardPrimaryButton(
                text = "Create Goal →",
                onClick = { onAction(OnboardingAction.OnFinish) },
                enabled = state.goalName.isNotBlank() && state.goalTarget.isNotBlank(),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StepIndicator(current: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(total) { index ->
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (index < current) Primary else SurfaceVariant),
                )
            }
        }
    }
}

@Preview
@Composable
private fun WelcomePreview() {
    GoalGuardTheme {
        WelcomeScreen(onAction = {})
    }
}

@Preview
@Composable
private fun ProfilePreview() {
    GoalGuardTheme {
        ProfileSetupScreen(state = OnboardingState(step = OnboardingStep.PROFILE), onAction = {})
    }
}
