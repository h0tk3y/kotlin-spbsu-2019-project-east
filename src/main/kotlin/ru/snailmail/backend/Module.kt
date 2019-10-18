package ru.snailmail.backend

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.receive
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
    routing {
        get("/") {
            call.respondText("west — lohi!", ContentType.Text.Plain)
        }
        get("/users") {
            call.respondText { objectMapper.writeValueAsString(Master.users) }
        }
        post("/register") {
            val user = call.receive<UserData>()
            try {
                Master.register(user.name, user.password)
            } catch (e: AlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict,
                    mapOf("error" to "User with login ${user.name} already exists"))
            }
            call.respondText("OK")
        }
        post("/login") {
            val user = call.receive<UserData>()
            val ret: User
            try {
                ret = Master.logIn(user.name, user.password)
            } catch (e: IllegalArgumentException) {
                call.respondText("ERROR")
                return@post
            } catch (e: DoesNotExistException) {
                call.respond(HttpStatusCode.Conflict,
                    mapOf("error" to "Wrong login"))
                return@post
            }
            call.respond(UserToken(ret.name, ret.password))
        }
    }
}