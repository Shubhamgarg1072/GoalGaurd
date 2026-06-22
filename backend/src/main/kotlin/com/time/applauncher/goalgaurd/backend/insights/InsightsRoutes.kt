package com.time.applauncher.goalgaurd.backend.insights

import com.time.applauncher.goalgaurd.backend.auth.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.datetime.LocalDate

fun Route.insightsRoutes(insights: InsightsService) {
    authenticate {
        get("/insights/summary") {
            val userId = call.principal<JWTPrincipal>()!!.userId()
            val from = call.request.queryParameters["from"]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            val to = call.request.queryParameters["to"]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            if (from == null || to == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "from and to (ISO dates) are required"))
                return@get
            }
            call.respond(insights.summary(userId, from, to))
        }
    }
}
