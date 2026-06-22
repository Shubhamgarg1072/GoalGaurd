package com.time.applauncher.goalgaurd.backend

import com.time.applauncher.goalgaurd.backend.auth.AuthException
import com.time.applauncher.goalgaurd.backend.auth.AuthService
import com.time.applauncher.goalgaurd.backend.auth.GoogleTokenVerifier
import com.time.applauncher.goalgaurd.backend.auth.JwtService
import com.time.applauncher.goalgaurd.backend.auth.authRoutes
import com.time.applauncher.goalgaurd.backend.coach.CoachService
import com.time.applauncher.goalgaurd.backend.coach.coachRoutes
import com.time.applauncher.goalgaurd.backend.config.AppConfig
import com.time.applauncher.goalgaurd.backend.db.DatabaseFactory
import com.time.applauncher.goalgaurd.backend.insights.InsightsService
import com.time.applauncher.goalgaurd.backend.insights.insightsRoutes
import com.time.applauncher.goalgaurd.backend.sync.SyncService
import com.time.applauncher.goalgaurd.backend.sync.syncRoutes
import com.time.applauncher.goalgaurd.shared.api.GoalGuardJson
import com.time.applauncher.goalgaurd.shared.model.ApiError
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    val config = AppConfig.fromEnv()
    DatabaseFactory.init(config.db)
    embeddedServer(Netty, port = config.port, host = "0.0.0.0") {
        module(config)
    }.start(wait = true)
}

fun Application.module(config: AppConfig) {
    val jwtService = JwtService(config)
    val googleVerifier = GoogleTokenVerifier.create(config)
    val authService = AuthService(config, jwtService, googleVerifier)
    val syncService = SyncService()
    val coachService = CoachService(config)
    val insightsService = InsightsService()

    install(ContentNegotiation) { json(GoalGuardJson) }
    install(CallLogging)

    install(Authentication) {
        jwt {
            verifier(jwtService.verifier)
            validate { credential ->
                if (credential.payload.getClaim("uid").asString() != null) JWTPrincipal(credential.payload) else null
            }
        }
    }

    install(StatusPages) {
        exception<AuthException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized, ApiError("unauthorized", cause.message ?: "Unauthorized"))
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled error", cause)
            call.respond(HttpStatusCode.InternalServerError, ApiError("server_error", "Something went wrong"))
        }
    }

    routing {
        get("/health") { call.respond(mapOf("status" to "ok")) }
        authRoutes(authService)
        syncRoutes(syncService)
        coachRoutes(coachService)
        insightsRoutes(insightsService)
    }
}
