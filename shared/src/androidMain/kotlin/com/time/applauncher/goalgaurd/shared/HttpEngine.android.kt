package com.time.applauncher.goalgaurd.shared.api

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android

actual fun defaultHttpEngine(): HttpClientEngine = Android.create()
