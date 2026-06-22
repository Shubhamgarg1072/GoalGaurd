package com.time.applauncher.goalgaurd.feature.onboarding.presentation

import android.app.AppOpsManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.time.applauncher.goalgaurd.core.designsystem.components.GoalGuardPrimaryButton
import com.time.applauncher.goalgaurd.core.designsystem.theme.BackgroundDeep
import com.time.applauncher.goalgaurd.core.designsystem.theme.Primary
import com.time.applauncher.goalgaurd.core.designsystem.theme.Success
import com.time.applauncher.goalgaurd.core.designsystem.theme.SurfaceVariant
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextMuted
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextPrimary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextSecondary

// ── Data ────────────────────────────────────────────────────────────────────

private data class PermissionsStatus(
    val isDefaultLauncher: Boolean,
    val hasUsageAccess: Boolean,
    val canDrawOverlays: Boolean,
    val hasNotifications: Boolean,
) {
    val allRequiredGranted get() = isDefaultLauncher && hasUsageAccess && canDrawOverlays
}

@Suppress("DEPRECATION")
private fun checkAllPermissions(context: Context): PermissionsStatus {
    // Default launcher: resolve the HOME intent and compare package names
    val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val resolved = context.packageManager.resolveActivity(homeIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
    val isDefaultLauncher = resolved?.activityInfo?.packageName == context.packageName

    // Usage access via AppOpsManager
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val opsMode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName,
    )
    val hasUsageAccess = opsMode == AppOpsManager.MODE_ALLOWED

    // Overlay permission
    val canDrawOverlays = Settings.canDrawOverlays(context)

    // Notifications (only runtime-requested on Android 13+)
    val hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    return PermissionsStatus(
        isDefaultLauncher = isDefaultLauncher,
        hasUsageAccess = hasUsageAccess,
        canDrawOverlays = canDrawOverlays,
        hasNotifications = hasNotifications,
    )
}

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun LauncherPermissionsScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var status by remember { mutableStateOf(checkAllPermissions(context)) }

    // Re-check every time the user returns from a system Settings screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                status = checkAllPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Launcher for POST_NOTIFICATIONS standard dialog
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { status = checkAllPermissions(context) }

    // Launcher for RoleManager.ROLE_HOME chooser (API 29+)
    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { status = checkAllPermissions(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
    ) {
        Spacer(Modifier.height(56.dp))

        Text(
            "Set up GoalGuard",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "A few permissions let GoalGuard guard your goals and protect your focus.",
            fontSize = 15.sp,
            color = TextSecondary,
            lineHeight = 22.sp,
        )

        Spacer(Modifier.height(32.dp))

        SectionLabel("REQUIRED")
        Spacer(Modifier.height(10.dp))

        PermissionCard(
            emoji = "🏠",
            title = "Default Launcher",
            description = "Makes GoalGuard your home screen so it can protect your focus.",
            isGranted = status.isDefaultLauncher,
            onGrant = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager =
                        context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                    if (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                        roleLauncher.launch(
                            roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME),
                        )
                    }
                } else {
                    context.startActivity(
                        Intent(Settings.ACTION_HOME_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        },
                    )
                }
            },
        )

        Spacer(Modifier.height(10.dp))

        PermissionCard(
            emoji = "📊",
            title = "Usage Access",
            description = "Tracks screen time to show doom-scroll alerts at the right moment.",
            isGranted = status.hasUsageAccess,
            onGrant = {
                context.startActivity(
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    },
                )
            },
        )

        Spacer(Modifier.height(10.dp))

        PermissionCard(
            emoji = "🛡️",
            title = "Display Over Apps",
            description = "Shows a goal reminder overlay when you open a distracting app.",
            isGranted = status.canDrawOverlays,
            onGrant = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}"),
                    ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK },
                )
            },
        )

        Spacer(Modifier.height(24.dp))

        SectionLabel("OPTIONAL")
        Spacer(Modifier.height(10.dp))

        PermissionCard(
            emoji = "🔔",
            title = "Notifications",
            description = "Habit reminders and daily goal nudges.",
            isGranted = status.hasNotifications,
            onGrant = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            hideGrantButton = status.hasNotifications ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU,
        )

        Spacer(Modifier.height(40.dp))

        if (status.allRequiredGranted) {
            GoalGuardPrimaryButton(text = "All Set! Continue →", onClick = onContinue)
        } else {
            GoalGuardPrimaryButton(
                text = "Continue",
                onClick = onContinue,
                enabled = false,
            )
            Spacer(Modifier.height(10.dp))
            TextButton(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Skip for now", color = TextMuted, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Components ───────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = if (text == "REQUIRED") Primary else TextMuted,
        letterSpacing = 1.sp,
    )
}

@Composable
private fun PermissionCard(
    emoji: String,
    title: String,
    description: String,
    isGranted: Boolean,
    onGrant: () -> Unit,
    hideGrantButton: Boolean = isGranted,
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
                .background(
                    if (isGranted) Success.copy(alpha = 0.15f) else Primary.copy(alpha = 0.12f),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 22.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp,
            )
        }

        Spacer(Modifier.width(10.dp))

        if (isGranted) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Success),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Granted",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp),
                )
            }
        } else if (!hideGrantButton) {
            OutlinedButton(
                onClick = onGrant,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text("Grant", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
