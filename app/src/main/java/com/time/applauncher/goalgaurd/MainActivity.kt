package com.time.applauncher.goalgaurd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.time.applauncher.goalgaurd.core.designsystem.theme.GoalGuardTheme
import com.time.applauncher.goalgaurd.feature.onboarding.domain.OnboardingRepository
import com.time.applauncher.goalgaurd.feature.vault.domain.VaultRepository
import com.time.applauncher.goalgaurd.feature.vault.domain.VaultStatus
import com.time.applauncher.goalgaurd.navigation.DashboardRoute
import com.time.applauncher.goalgaurd.navigation.GoalGuardNavHost
import com.time.applauncher.goalgaurd.navigation.OnboardingRoute
import com.time.applauncher.goalgaurd.navigation.VaultSetupRoute
import com.time.applauncher.goalgaurd.navigation.VaultUnlockRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val onboardingRepository: OnboardingRepository by inject()
    private val vaultRepository: VaultRepository by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        // mutableStateOf so Compose tracks this and recomposes when it changes
        var isReady by mutableStateOf(false)
        var startDestination: Any by mutableStateOf(OnboardingRoute)

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !isReady }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        scope.launch {
            val onboardingComplete = onboardingRepository.isOnboardingComplete().first()
            startDestination = when {
                !onboardingComplete -> OnboardingRoute
                // Returning user with an encrypted vault: gate the app behind unlock / first-time set-up.
                vaultRepository.currentStatus() == VaultStatus.NOT_SET_UP -> VaultSetupRoute
                vaultRepository.currentStatus() == VaultStatus.LOCKED -> VaultUnlockRoute
                else -> DashboardRoute
            }
            isReady = true  // triggers both Compose recomposition AND splash dismissal
        }

        setContent {
            GoalGuardTheme {
                if (isReady) {
                    GoalGuardNavHost(startDestination = startDestination)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
