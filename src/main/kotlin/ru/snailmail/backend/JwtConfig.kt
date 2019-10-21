package ru.snailmail.backend

import com.auth0.jwt.*
import com.auth0.jwt.algorithms.*
import io.ktor.auth.UserPasswordCredential
import java.util.*

object JwtConfig {
    private const val issuer = "ktor.io"
    private val algorithm = Algorithm.HMAC512("Save a tree. Eat a beaver.")

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    fun makeToken(userId: UID, data: UserPasswordCredential): String = JWT.create()
        .withSubject(userId.id.toString())
        .withIssuer(issuer)
        .withClaim("id", userId.toString())
        .withClaim("name", data.name)
        .withClaim("password", data.password)
        .withExpiresAt(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24h expiration time
        .sign(algorithm)
}