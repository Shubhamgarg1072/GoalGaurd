package com.time.applauncher.goalgaurd.backend.auth

import com.time.applauncher.goalgaurd.shared.model.GoogleSignInRequest
import com.time.applauncher.goalgaurd.shared.model.RefreshRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

/** Pulls the authenticated user id from the validated JWT. */
fun JWTPrincipal.userId(): String = payload.getClaim("uid").asString()

fun Route.authRoutes(auth: AuthService) {
    post("/auth/google") {
        val body = call.receive<GoogleSignInRequest>()
        call.respond(auth.signInWithGoogle(body.idToken))
    }

    post("/auth/refresh") {
        val body = call.receive<RefreshRequest>()
        call.respond(auth.refresh(body.refreshToken))
    }

    authenticate {
        get("/me") {
            val userId = call.principal<JWTPrincipal>()!!.userId()
            val user = auth.userById(userId)
            if (user == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(user)
        }
    }
}
