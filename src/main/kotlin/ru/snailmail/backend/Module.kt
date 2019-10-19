package ru.snailmail.backend

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.request.receive
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.auth.*
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import kotlin.IllegalArgumentException

private val objectMapper = jacksonObjectMapper()

data class CreateLichkaRequest(val token: String, val invitedId: String)

fun Application.module() {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(Routing) {
        get("/") {
            call.respondText("west — lohi!", ContentType.Text.Plain)
        }
        get("/users") {
            call.respondText { objectMapper.writeValueAsString(Master.users) + "\n" }
        }
        post("/register") {
            val creds = call.receive<UserPasswordCredential>()
            try {
                Master.register(creds)
            } catch (e: AlreadyExistsException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("error" to "User with this login already exists")
                )
            }
            call.respondText("OK\n")
        }
        post("/login") {
            val creds = call.receive<UserPasswordCredential>()
            try {
                val user = Master.logIn(creds)
                val token = JwtConfig.makeToken(user.userID, creds)
                call.respondText("Your token: $token\n")
            } catch (e: IllegalArgumentException) {
                call.respondText("ERROR\n")
                return@post
            } catch (e: DoesNotExistException) {
                call.respond(HttpStatusCode.Conflict,
                    mapOf("error" to "Wrong login"))
                return@post
            }
        }
        post("/createLichka") {
            val params = call.receive<CreateLichkaRequest>()
            log.info("params: $params")
            val token = params.token
            val fstUser: User
            val sndUser: User
            try {
                sndUser = Master.searchUserById(params.invitedId.toInt()) ?: throw IllegalArgumentException()
                val userId = JwtConfig.verifier.verify(token).subject.drop(7).dropLast(1).toInt() // TODO: fix this
                fstUser = Master.searchUserById(userId) ?: throw IllegalAccessException()
                Master.createLichka(fstUser, sndUser)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                return@post
            }
            call.respondText("OK\n")
        }
    }
}