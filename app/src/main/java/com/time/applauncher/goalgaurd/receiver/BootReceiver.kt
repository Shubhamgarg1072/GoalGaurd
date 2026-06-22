package com.time.applauncher.goalgaurd.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.time.applauncher.goalgaurd.feature.guard.domain.GuardConfigRepository
import com.time.applauncher.goalgaurd.feature.guard.presentation.DoomScrollGuardService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val guardConfigRepository: GuardConfigRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // Android automatically re-launches the default home app after boot via the CATEGORY_HOME
        // intent filter. Here we also restart the doom-scroll guard if the user had it enabled —
        // foreground services are killed on reboot.
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                if (guardConfigRepository.currentConfig().enabled) {
                    DoomScrollGuardService.start(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
