package com.time.applauncher.goalgaurd.di

import com.time.applauncher.goalgaurd.BuildConfig
import com.time.applauncher.goalgaurd.feature.auth.data.DataStoreTokenStore
import com.time.applauncher.goalgaurd.feature.auth.data.GoogleCredentialProvider
import com.time.applauncher.goalgaurd.shared.api.GoalGuardApi
import com.time.applauncher.goalgaurd.shared.api.TokenProvider
import com.time.applauncher.goalgaurd.shared.api.createGoalGuardApi
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val networkModule = module {
    single { DataStoreTokenStore(androidContext()) }
    single<TokenProvider> { get<DataStoreTokenStore>() }
    single<GoalGuardApi> {
        createGoalGuardApi(baseUrl = BuildConfig.BASE_URL, tokens = get<DataStoreTokenStore>())
    }
    single { GoogleCredentialProvider(BuildConfig.GOOGLE_WEB_CLIENT_ID) }
}
