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
import io.ktor.html.respondHtml
import io.ktor.jackson.jackson
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.title
import kotlin.IllegalArgumentException

private val objectMapper = jacksonObjectMapper()

data class CreateLichkaRequest(val token: String, val invitedId: UID)

data class SendMessageRequest(val token: String, val chatId: UID, val text: String)

data class TokenRequest(val token: String)

data class TokenMessageRequest(val token: String, val chatId: UID, val messageId: UID)

data class CreatePublicChatRequest(val token: String, val chatName: String)

data class InviteMemberRequest(val token: String, val chatId: UID, val invitedId: UID)

data class ChangeNameRequest(val token: String, val userId: UID, val newName: String)

data class BlockOrUnblockUserRequest(val token: String, val userId: UID)

data class AddOrDeleteContactRequest(val token: String, val userId: UID)

fun userByToken(token: String): User {
    val userId = JwtConfig.verifier.verify(token).subject.toLong()
    return Master.findUserById(UID(userId)) ?: throw IllegalAccessException()
}

fun Application.module() {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(Routing) {
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
                call.respondText("Your token: $token\n")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
            }
        }
        post("/createLichka") {
            try {
                val params = call.receive<CreateLichkaRequest>()
                val sndUser = Master.findUserById(params.invitedId) ?: throw IllegalArgumentException()
                val fstUser = userByToken(params.token)
                val chatId = Master.createLichka(fstUser, sndUser)
                call.respondText("LichkaId: $chatId\n")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
            }
        }
        post("/createPublicChat") {
            try {
                val params = call.receive<CreatePublicChatRequest>()
                val owner = userByToken(params.token)
                val chatId = Master.createPublicChat(owner, params.chatName)
                call.respondText("PublicChatId: $chatId\n")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
            }
        }
        post("/inviteMember") {
            try {
                val params = call.receive<InviteMemberRequest>()
                val user = userByToken(params.token)
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
                val user = userByToken(params.token)
                val chat = Master.findChatById(params.chatId) ?: throw java.lang.IllegalArgumentException()
                val msgId = Master.sendMessage(user, chat, params.text)
                call.respondText("Message id: $msgId\n")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
            }
        }
        post("/chats") {
            // TODO: hide chats with blocked users.
            try {
                val params = call.receive<TokenRequest>()
                val user = userByToken(params.token)
                call.respondText(objectMapper.writeValueAsString(user.chats) + "\n")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
            }
        }
        post("/deleteMessage") {
            try {
                val params = call.receive<TokenMessageRequest>()
                val user = userByToken(params.token)
                Master.deleteMessage(user, Master.findChatById(params.chatId)!!, params.messageId)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
            }
        }
        post("/contacts") {
            try {
                val params = call.receive<TokenRequest>()
                val user = userByToken(params.token)
                call.respondText { objectMapper.writeValueAsString(user.contacts.values) + "\n" }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
            }
        }
        post("/changeContactName") {
            try {
                val params = call.receive<ChangeNameRequest>()
                val user = userByToken(params.token)
                val contact = user.contacts[params.userId] ?: throw java.lang.IllegalArgumentException()
                contact.preferredName = params.newName
                call.respondText("OK")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
            }
        }
        post("/blockUser") {
            try {
                val params = call.receive<BlockOrUnblockUserRequest>()
                val user = userByToken(params.token)
                val contact = user.contacts[params.userId] ?: throw java.lang.IllegalArgumentException()
                contact.isBlocked = true
                call.respondText("OK")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
            }
        }
        post("/unblockUser") {
            try {
                val params = call.receive<BlockOrUnblockUserRequest>()
                val user = userByToken(params.token)
                val contact = user.contacts[params.userId] ?: throw java.lang.IllegalArgumentException()
                contact.isBlocked = false
                call.respondText("OK")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
            }
        }
        post("addContact") {
            try {
                val params = call.receive<AddOrDeleteContactRequest>()
                val user = userByToken(params.token)
                val other = Master.findUserById(params.userId) ?: throw java.lang.IllegalArgumentException()
                user.addContact(other)
                call.respondText("OK")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
            }
        }
        post("deleteContact") {
            try {
                val params = call.receive<AddOrDeleteContactRequest>()
                val user = userByToken(params.token)
                val other = Master.findUserById(params.userId) ?: throw java.lang.IllegalArgumentException()
                user.deleteContact(other)
                call.respondText("OK")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
            }
        }
    }
}