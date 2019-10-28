package ru.snailmail.backend

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.request.receive
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.auth.*
import io.ktor.auth.jwt.jwt
import io.ktor.features.ContentNegotiation
import io.ktor.html.respondHtml
import io.ktor.jackson.jackson
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.title
import kotlin.IllegalArgumentException

private val objectMapper = jacksonObjectMapper()

data class CreateLichkaRequest(val invitedId: UID)

data class SendMessageRequest(val chatId: UID, val text: String)

data class TokenRequest(val token: String)

data class TokenMessage(val chatId: UID, val messageId: UID)

data class CreatePublicChatRequest(val chatName: String)

data class InviteMemberRequest(val userId: UID, val chatId: UID, val invitedId: UID)


fun Application.module() {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(Authentication) {
        jwt {
            verifier(JwtConfig.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }

    install(Routing) {
        // TODO: fix exceptions handling.
        get("/") {
            call.respondHtml {
                head {
                    title("West - Lohi!")
                }
                body {
                    h1 { +"West - lohi!" }
                }
            }
        }
        get("/users") {
            call.respondText { objectMapper.writeValueAsString(Master.users) + "\n" }
        }
        post("/register") {
            try {
                val creds = call.receive<UserPasswordCredential>()
                val userId = Master.register(creds)
                call.respondText("User id: $userId\n")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
            }
        }
        post("/login") {
            try {
                val creds = call.receive<UserPasswordCredential>()
                val user = Master.logIn(creds)
                val token = JwtConfig.makeToken(user.userID, creds)
                call.respond(token)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
            }
        }
        authenticate {
            post("/createLichka") {
                try {
                    val params = call.receive<CreateLichkaRequest>()
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val sndUser = Master.findUserById(params.invitedId) ?: throw IllegalArgumentException()
                    val fstUser = Master.findUserByLogin(principal.name) ?: throw DoesNotExistException()
                    val chatId = Master.createLichka(fstUser, sndUser)
                    call.respondText("LichkaId: $chatId\n")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
                }
            }
            post("/createPublicChat") {
                try {
                    val params = call.receive<CreatePublicChatRequest>()
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val owner = Master.findUserByLogin(principal.name) ?: throw DoesNotExistException()
                    val chatId = Master.createPublicChat(owner, params.chatName)
                    call.respondText("PublicChatId: $chatId\n")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                }
            }
            post("/inviteMember") {
                try {
                    val params = call.receive<InviteMemberRequest>()
                    val user = Master.findUserById(params.userId) ?: throw IllegalArgumentException()
                    val invited = Master.findUserById(params.invitedId) ?: throw IllegalArgumentException()
                    val chat = Master.findChatById(params.chatId) ?: throw IllegalArgumentException()
                    Master.inviteUser(user, chat as PublicChat, invited)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                }
            }

            post("/sendMessage") {
                try {
                    val params = call.receive<SendMessageRequest>()
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name) ?: throw DoesNotExistException()
                    val chat = Master.findChatById(params.chatId) ?: throw java.lang.IllegalArgumentException()
                    val msgId = Master.sendMessage(user, chat, params.text)
                    call.respondText("Message id: $msgId\n")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
                }
            }
            post("/showChats") {
                try {
                    val params = call.receive<TokenRequest>()
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name) ?: throw DoesNotExistException()
                    call.respondText(objectMapper.writeValueAsString(user.chats) + "\n")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
                }
            }
            post("/deleteMessage") {
                try {
                    val params = call.receive<TokenMessage>()
                    Master.deleteMessage(Master.findChatById(params.chatId)!!, params.messageId)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
                }
            }
        }
    }
}