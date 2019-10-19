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

data class SendMessageRequest(val token: String, val chatId: String, val text: String)

data class TokenRequest(val token: String)

fun userByToken(token: String): User {
    val userId = JwtConfig.verifier.verify(token).subject.drop(7).dropLast(1).toInt() // TODO: fix this
    return Master.findUserById(userId) ?: throw IllegalAccessException()
}

fun Application.module() {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(Routing) {
        // TODO: fix exceptions handling.
        get("/") {
            call.respondText("west — lohi!", ContentType.Text.Plain)
        }
        get("/users") {
            call.respondText { objectMapper.writeValueAsString(Master.users) + "\n" }
        }
        post("/register") {
            try {
                val creds = call.receive<UserPasswordCredential>()
                val userId = Master.register(creds)
                call.respondText("User id: $userId\n")
            } catch (e: AlreadyExistsException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("error" to "User with this login already exists")
                )
            }
        }
        post("/login") {
            try {
                val creds = call.receive<UserPasswordCredential>()
                val user = Master.logIn(creds)
                val token = JwtConfig.makeToken(user.userID, creds)
                call.respondText("Your token: $token\n")
            } catch (e: IllegalArgumentException) {
                call.respondText("ERROR\n")
            } catch (e: DoesNotExistException) {
                call.respond(HttpStatusCode.Conflict,
                    mapOf("error" to "Wrong login"))
            }
        }
        post("/createLichka") {
            try {
                val params = call.receive<CreateLichkaRequest>()
                val sndUser = Master.findUserById(params.invitedId.toInt()) ?: throw IllegalArgumentException()
                val fstUser = userByToken(params.token)
                val chatId = Master.createLichka(fstUser, sndUser)
                call.respondText("LichkaId: $chatId\n")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
            }
        }
        post("/sendMessage") {
            try {
                val params = call.receive<SendMessageRequest>()
                val user = userByToken(params.token)
                val chat = Master.findChatById(params.chatId.toInt()) ?: throw java.lang.IllegalArgumentException()
                val msgId = Master.sendMessage(user, chat, params.text)
                call.respondText("Message id: $msgId\n")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
            }
        }
        post("/showChats") {
            try {
                val params = call.receive<TokenRequest>()
                val user = userByToken(params.token)
                call.respondText(objectMapper.writeValueAsString(user.chats) + "\n")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
            }
        }
    }
}