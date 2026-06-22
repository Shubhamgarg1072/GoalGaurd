package com.time.applauncher.goalgaurd.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.time.applauncher.goalgaurd.backend.config.AppConfig
import java.util.Date

class JwtService(private val config: AppConfig) {
    private val algorithm = Algorithm.HMAC256(config.jwtSecret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(config.jwtIssuer)
        .withAudience(config.jwtAudience)
        .build()

    fun createAccessToken(userId: String): String =
        JWT.create()
            .withIssuer(config.jwtIssuer)
            .withAudience(config.jwtAudience)
            .withSubject(userId)
            .withClaim("uid", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + config.accessTokenMinutes * 60_000))
            .sign(algorithm)
}
