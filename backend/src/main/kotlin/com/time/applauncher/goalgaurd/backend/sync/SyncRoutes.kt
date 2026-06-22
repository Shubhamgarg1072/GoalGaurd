package com.time.applauncher.goalgaurd.backend.sync

import com.time.applauncher.goalgaurd.backend.auth.userId
import com.time.applauncher.goalgaurd.shared.model.SyncRequest
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.syncRoutes(sync: SyncService) {
    authenticate {
        post("/sync") {
            val userId = call.principal<JWTPrincipal>()!!.userId()
            val request = call.receive<SyncRequest>()
            call.respond(sync.sync(userId, request))
        }
    }
}
