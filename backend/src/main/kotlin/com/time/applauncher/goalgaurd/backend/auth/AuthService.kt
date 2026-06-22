package com.time.applauncher.goalgaurd.backend.auth

import com.time.applauncher.goalgaurd.backend.config.AppConfig
import com.time.applauncher.goalgaurd.backend.db.DatabaseFactory.dbQuery
import com.time.applauncher.goalgaurd.backend.db.RefreshTokens
import com.time.applauncher.goalgaurd.backend.db.Users
import com.time.applauncher.goalgaurd.shared.model.AuthResponse
import com.time.applauncher.goalgaurd.shared.model.UserDto
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import java.time.Instant
import java.util.UUID

class AuthException(message: String) : RuntimeException(message)

class AuthService(
    private val config: AppConfig,
    private val jwt: JwtService,
    private val google: GoogleTokenVerifier,
) {
    suspend fun signInWithGoogle(idToken: String): AuthResponse {
        val identity = google.verify(idToken) ?: throw AuthException("Invalid Google ID token")
        val userId = "g_${identity.subject}"
        val user = dbQuery {
            Users.upsert(Users.id) {
                it[id] = userId
                it[email] = identity.email
                it[displayName] = identity.name
                it[pictureUrl] = identity.pictureUrl
                it[createdAt] = Instant.now()
            }
            UserDto(userId, identity.email, identity.name, identity.pictureUrl)
        }
        return issueTokens(user)
    }

    suspend fun refresh(refreshToken: String): AuthResponse {
        val userId = dbQuery {
            val row = RefreshTokens
                .selectAll()
                .where { (RefreshTokens.token eq refreshToken) and (RefreshTokens.expiresAt greater Instant.now()) }
                .singleOrNull() ?: throw AuthException("Invalid or expired refresh token")
            // Rotate: delete the consumed token.
            RefreshTokens.deleteWhere { token eq refreshToken }
            row[RefreshTokens.userId]
        }
        val user = userById(userId) ?: throw AuthException("User no longer exists")
        return issueTokens(user)
    }

    suspend fun userById(userId: String): UserDto? = dbQuery {
        Users.selectAll().where { Users.id eq userId }.singleOrNull()?.toUserDto()
    }

    private suspend fun issueTokens(user: UserDto): AuthResponse {
        val refresh = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "")
        dbQuery {
            RefreshTokens.insert {
                it[token] = refresh
                it[userId] = user.id
                it[createdAt] = Instant.now()
                it[expiresAt] = Instant.now().plusSeconds(config.refreshTokenDays * 86_400)
            }
        }
        return AuthResponse(
            user = user,
            accessToken = jwt.createAccessToken(user.id),
            refreshToken = refresh,
        )
    }

    private fun ResultRow.toUserDto() = UserDto(
        id = this[Users.id],
        email = this[Users.email],
        displayName = this[Users.displayName],
        pictureUrl = this[Users.pictureUrl],
    )
}
