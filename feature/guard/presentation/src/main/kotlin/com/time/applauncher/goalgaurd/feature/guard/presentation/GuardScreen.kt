package com.time.applauncher.goalgaurd.feature.guard.presentation

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.time.applauncher.goalgaurd.core.designsystem.components.GoalGuardCard
import com.time.applauncher.goalgaurd.core.designsystem.theme.BackgroundDeep
import com.time.applauncher.goalgaurd.core.designsystem.theme.Primary
import com.time.applauncher.goalgaurd.core.designsystem.theme.Success
import com.time.applauncher.goalgaurd.core.designsystem.theme.SurfaceVariant
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextMuted
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextPrimary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextSecondary
import com.time.applauncher.goalgaurd.core.presentation.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun GuardRoot(
    onNavigateBack: () -> Unit,
    viewModel: GuardViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            GuardEvent.StartService -> DoomScrollGuardService.start(context)
            GuardEvent.StopService -> DoomScrollGuardService.stop(context)
        }
    }

    GuardScreen(state = state, onAction = viewModel::onAction, onNavigateBack = onNavigateBack)
}

@Composable
fun GuardScreen(
    state: GuardState,
    onAction: (GuardAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var perms by remember { mutableStateOf(checkGuardPermissions(context)) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) perms = checkGuardPermissions(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text("Doom-scroll Guard", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Stay on track when apps pull you in", fontSize = 12.sp, color = TextSecondary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            val permsReady = perms.hasUsageAccess && perms.canDrawOverlays

            // Guard toggle
            GoalGuardCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Guard mode", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            if (permsReady) "Watch monitored apps and intervene"
                            else "Grant the permissions below to enable",
                            fontSize = 12.sp,
                            color = TextSecondary,
                        )
                    }
                    Switch(
                        checked = state.enabled,
                        onCheckedChange = { onAction(GuardAction.SetEnabled(it)) },
                        enabled = permsReady || state.enabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Primary,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Threshold stepper
            GoalGuardCard(modifier = Modifier.fillMaxWidth()) {
                Text("Scroll threshold", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Intervene after this much continuous scrolling", fontSize = 12.sp, color = TextSecondary)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StepperButton("−") {
                        onAction(GuardAction.SetThreshold(state.continuousMinutesThreshold - 5))
                    }
                    Text(
                        "${state.continuousMinutesThreshold} min",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        modifier = Modifier.weight(1f),
                    )
                    StepperButton("+") {
                        onAction(GuardAction.SetThreshold(state.continuousMinutesThreshold + 5))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("${state.monitoredAppCount} apps monitored", fontSize = 11.sp, color = TextMuted)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "PERMISSIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(10.dp))

            GuardPermissionCard(
                emoji = "📊",
                title = "Usage Access",
                description = "Lets Guard see which app is in the foreground and for how long.",
                isGranted = perms.hasUsageAccess,
                onGrant = {
                    context.startActivity(
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        },
                    )
                },
            )
            Spacer(Modifier.height(10.dp))
            GuardPermissionCard(
                emoji = "🛡️",
                title = "Display Over Apps",
                description = "Lets Guard show the intervention over the distracting app.",
                isGranted = perms.canDrawOverlays,
                onGrant = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}"),
                        ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK },
                    )
                },
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick) {
            Text(label, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Primary)
        }
    }
}

@Composable
private fun GuardPermissionCard(
    emoji: String,
    title: String,
    description: String,
    isGranted: Boolean,
    onGrant: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceVariant)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isGranted) Success.copy(alpha = 0.15f) else Primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 22.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(description, fontSize = 12.sp, color = TextSecondary, lineHeight = 17.sp)
        }
        Spacer(Modifier.width(10.dp))
        if (isGranted) {
            Box(
                modifier = Modifier.size(30.dp).clip(CircleShape).background(Success),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Check, contentDescription = "Granted", tint = Color.White, modifier = Modifier.size(17.dp))
            }
        } else {
            OutlinedButton(
                onClick = onGrant,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
            ) {
                Text("Grant", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Permission status ─────────────────────────────────────────────────────────

private data class GuardPermissions(
    val hasUsageAccess: Boolean,
    val canDrawOverlays: Boolean,
)

private fun checkGuardPermissions(context: Context): GuardPermissions {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val opsMode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName,
    )
    return GuardPermissions(
        hasUsageAccess = opsMode == AppOpsManager.MODE_ALLOWED,
        canDrawOverlays = Settings.canDrawOverlays(context),
    )
}
