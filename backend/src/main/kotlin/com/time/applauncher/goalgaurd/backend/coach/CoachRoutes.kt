package com.time.applauncher.goalgaurd.backend.coach

import com.time.applauncher.goalgaurd.shared.model.CoachInputDto
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.coachRoutes(coach: CoachService) {
    authenticate {
        post("/coach/generate") {
            val input = call.receive<CoachInputDto>()
            call.respond(coach.generate(input))
        }
    }
}
