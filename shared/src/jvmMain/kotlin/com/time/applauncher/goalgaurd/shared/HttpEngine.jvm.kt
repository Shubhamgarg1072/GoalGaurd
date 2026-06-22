package com.time.applauncher.goalgaurd.shared.api

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

actual fun defaultHttpEngine(): HttpClientEngine = CIO.create()
