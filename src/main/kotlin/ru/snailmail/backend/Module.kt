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

data class DeleteMessageRequest(val chatId: UID, val messageId: UID)

data class ShowMessageRequest(val chatId: UID)

data class CreatePublicChatRequest(val chatName: String)

data class InviteMemberRequest(val chatId: UID, val invitedId: UID)

data class ChangeNameRequest(val userId: UID, val newName: String)

data class BlockOrUnblockUserRequest(val userId: UID)

data class AddOrDeleteContactRequest(val userId: UID)


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
            call.respond(Data.getUsers().map { it.name })
        }
        post("/register") {
            try {
                val creds = call.receive<UserPasswordCredential>()
                Master.register(creds)
                call.respondText("OK")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "error: ".plus(e.message))
            }
        }
        post("/login") {
            try {
                val creds = call.receive<UserPasswordCredential>()
                val uid = Master.logIn(creds)
                val token = JwtConfig.makeToken(uid, creds)
                call.respond(token)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
            }
        }
        authenticate {
            post("/createLichka") {
                try {
                    val params = call.receive<CreateLichkaRequest>()
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val sndUser = Master.findUserById(params.invitedId) ?: throw IllegalArgumentException()
                    val fstUser = Master.findUserByLogin(principal.name) ?: throw DoesNotExistException()
                    Master.createLichka(fstUser, sndUser)
                    call.respondText("OK")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
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
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }
            post("/inviteMember") {
                try {
                    val params = call.receive<InviteMemberRequest>()
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    val invited = Master.findUserById(params.invitedId) ?: throw IllegalArgumentException()
                    val chat = Master.findChatById(params.chatId) ?: throw IllegalArgumentException()
                    Master.inviteUser(user, chat as PublicChat, invited)
                    call.respondText("OK")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }
            post("/sendMessage") {
                try {
                    val params = call.receive<SendMessageRequest>()
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    val chat = Master.findChatById(params.chatId) ?: throw java.lang.IllegalArgumentException()
                    Master.sendMessage(user, chat, params.text)
                    call.respondText("OK")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
                }
            }
            post("/chats") {
                try {
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    call.respond(Data.getUserChats(user.userID))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
                }
            }
            post("/showMessages") {
                try {
                    val params = call.receive<ShowMessageRequest>()
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    val chat = Master.findChatById(params.chatId) ?: throw java.lang.IllegalArgumentException()
                    if (!Data.findChatMembers(chat.chatID).contains(user)) {
                        throw DoesNotExistException("User not in the chat")
                    }
                    call.respond(Data.findChatMessages(chat.chatID).map { it.text })
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
                }
            }
            post("/deleteMessage") {
                try {
                    val params = call.receive<DeleteMessageRequest>()
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    Master.deleteMessage(user, Master.findChatById(params.chatId)!!, params.messageId)
                    call.respond("OK")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
                }
            }
            post("/contacts") {
                try {
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    call.respond(Data.getUserContacts(user.userID))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
                }
            }
            post("/changeContactName") {
                try {
                    val params = call.receive<ChangeNameRequest>()
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    if (!Data.changePreferredName(user.userID, params.userId, params.newName))
                        throw java.lang.IllegalArgumentException()
                    call.respondText("OK")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
                }
            }
            post("/blockUser") {
                try {
                    val params = call.receive<BlockOrUnblockUserRequest>()
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException("User not found")
                    if (!Data.blockUser(user.userID, params.userId))
                        throw IllegalArgumentException("Contact not found")
                    call.respondText("OK")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
                }
            }
            post("/unblockUser") {
                try {
                    val params = call.receive<BlockOrUnblockUserRequest>()
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    if (!Data.unblockUser(user.userID, params.userId))
                        throw IllegalArgumentException("Contact not found")
                    call.respondText("OK")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
                }
            }
            post("addContact") {
                try {
                    val params = call.receive<AddOrDeleteContactRequest>()
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    val other = Master.findUserById(params.userId) ?: throw java.lang.IllegalArgumentException()
                    Data.addContact(user.userID, other.userID)
                    call.respondText("OK")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
                }
            }
            post("deleteContact") {
                try {
                    val params = call.receive<AddOrDeleteContactRequest>()
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    val other = Master.findUserById(params.userId) ?: throw java.lang.IllegalArgumentException()
                    Data.deleteContact(user.userID, other.userID)
                    call.respondText("OK")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
                }
            }
        }
    }
}