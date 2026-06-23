package com.time.applauncher.goalgaurd

import android.app.Application
import com.time.applauncher.goalgaurd.di.authModule
import com.time.applauncher.goalgaurd.di.backupModule
import com.time.applauncher.goalgaurd.di.coachModule
import com.time.applauncher.goalgaurd.di.networkModule
import com.time.applauncher.goalgaurd.di.databaseModule
import com.time.applauncher.goalgaurd.di.dashboardModule
import com.time.applauncher.goalgaurd.di.focusModule
import com.time.applauncher.goalgaurd.di.goalsModule
import com.time.applauncher.goalgaurd.di.guardModule
import com.time.applauncher.goalgaurd.di.habitsModule
import com.time.applauncher.goalgaurd.di.onboardingModule
import com.time.applauncher.goalgaurd.di.vaultModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GoalGuardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GoalGuardApp)
            modules(
                databaseModule,
                onboardingModule,
                goalsModule,
                habitsModule,
                dashboardModule,
                focusModule,
                coachModule,
                backupModule,
                guardModule,
                networkModule,
                authModule,
                vaultModule,
            )
        }
    }
}
