package com.time.applauncher.goalgaurd.feature.backup.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.time.applauncher.goalgaurd.core.designsystem.components.GoalGuardCard
import com.time.applauncher.goalgaurd.core.designsystem.theme.BackgroundDeep
import com.time.applauncher.goalgaurd.core.designsystem.theme.Primary
import com.time.applauncher.goalgaurd.core.designsystem.theme.PrimaryContainer
import com.time.applauncher.goalgaurd.core.designsystem.theme.SuccessLight
import com.time.applauncher.goalgaurd.core.designsystem.theme.SurfaceVariant
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextMuted
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextPrimary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextSecondary
import com.time.applauncher.goalgaurd.core.presentation.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun BackupRoot(
    onNavigateBack: () -> Unit,
    onNavigateToGuard: () -> Unit,
    viewModel: BackupViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { viewModel.onAction(BackupAction.OnExportUriReceived(it)) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.onAction(BackupAction.OnImportUriReceived(it)) }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            BackupEvent.LaunchExportPicker -> exportLauncher.launch("goalguard-backup.json")
            BackupEvent.LaunchImportPicker -> importLauncher.launch(arrayOf("application/json"))
        }
    }

    BackupScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        onNavigateToGuard = onNavigateToGuard,
    )
}

@Composable
fun BackupScreen(
    state: BackupState,
    onAction: (BackupAction) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToGuard: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text("Backup & Restore", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Your data stays on your device", fontSize = 12.sp, color = TextSecondary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // Last export status
            state.lastExportMs?.let { ms ->
                val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a").withZone(ZoneId.systemDefault())
                val formatted = formatter.format(Instant.ofEpochMilli(ms))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SuccessLight.copy(alpha = 0.08f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Text("Last export: $formatted", fontSize = 12.sp, color = SuccessLight)
                }
                Spacer(Modifier.height(14.dp))
            }

            // Export card
            GoalGuardCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Export backup", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Save a JSON file to your storage", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { onAction(BackupAction.OnExportClick) },
                    enabled = !state.isExporting && !state.isImporting,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (state.isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BackgroundDeep, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Exporting…", fontSize = 14.sp)
                    } else {
                        Text("Export backup", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Restore card
            GoalGuardCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Restore backup", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Newer local data is never overwritten", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { onAction(BackupAction.OnImportClick) },
                    enabled = !state.isExporting && !state.isImporting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (state.isImporting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Primary, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Restoring…", fontSize = 14.sp, color = Primary)
                    } else {
                        Text("Restore from file", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Primary)
                    }
                }
            }

            // Status message
            state.message?.let { msg ->
                Spacer(Modifier.height(14.dp))
                Text(msg, fontSize = 13.sp, color = TextMuted, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(24.dp))

            // Doom-scroll Guard entry (Phase 3)
            GoalGuardCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToGuard() },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Doom-scroll Guard", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Intervene when distracting apps pull you in", fontSize = 12.sp, color = TextSecondary)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Info card
            GoalGuardCard(modifier = Modifier.fillMaxWidth()) {
                Text("How backup works", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                listOf(
                    "Your data is exported as a JSON file to storage you control.",
                    "On restore, newer local records always win (last-write-wins).",
                    "Habit logs are merged by union — no entry is ever lost.",
                    "No account or internet required.",
                ).forEach { line ->
                    Spacer(Modifier.height(4.dp))
                    Text("• $line", fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
