package ru.snailmail.backend

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.auth.*
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import java.lang.IllegalArgumentException

private val objectMapper = jacksonObjectMapper()

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
            call.respondText { objectMapper.writeValueAsString(Master.users) }
        }
        // TODO: fix get -> post.
        get("/register") {
            val creds = call.request.queryParameters
            try {
                val name = creds["name"] ?: throw IllegalArgumentException()
                val password = creds["password"] ?: throw IllegalArgumentException()
                Master.register(UserPasswordCredential(name, password))
            } catch (e: AlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict,
                    mapOf("error" to "User with this login already exists"))
            }
            call.respondText("OK")
        }
        get("/login") {
            val creds = call.request.queryParameters
            try {
                val name = creds["name"] ?: throw IllegalArgumentException()
                val password = creds["password"] ?: throw IllegalArgumentException()
                val userCreds = UserPasswordCredential(name, password)
                val user = Master.logIn(userCreds)
                val token = JwtConfig.makeToken(user.userID, userCreds)
                call.respondText("Your token: $token")
            } catch (e: IllegalArgumentException) {
                call.respondText("ERROR")
                return@get
            } catch (e: DoesNotExistException) {
                call.respond(HttpStatusCode.Conflict,
                    mapOf("error" to "Wrong login"))
                return@get
            }
        }

        get("/createLichka") {
            val params = call.request.queryParameters
            if (params.contains("token") && params.contains("id")) {
                val token = params["token"]
                val invitedId = params["id"]!!.toInt()
                val fstUser: User
                val sndUser: User
                try {
                    val userId = JwtConfig.verifier.verify(token).subject.drop(7).dropLast(1).toInt() // TODO: fix this
                    fstUser = Master.searchUserById(userId) ?: throw IllegalAccessException()
                } catch (e: Exception) {
                    call.respondText(e.message!! + '\n')
                    return@get
                }
                if (Master.searchUserById(invitedId) == null) {
                    call.respondText("Invited user doesn't exist\n")
                    return@get
                }
                sndUser = Master.searchUserById(invitedId)!!
                try {
                    Master.createLichka(fstUser, sndUser)
                } catch (e: AlreadyInTheChatException) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                    return@get
                } catch (e: AlreadyExistsException) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        mapOf("error" to e.message)
                    )
                    return@get
                }
                call.respondText("OK\n")
            } else {
                call.respondText("Incorrect format\n")
            }
        }
    }
}