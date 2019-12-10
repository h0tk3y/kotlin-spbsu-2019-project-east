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

suspend inline fun <reified T : Any> requestData(f : (T, UserIdPrincipal) -> Unit, call : ApplicationCall) {
    try {
        val params = call.receive<T>()
        val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
        f(params, principal)
    } catch (e : Exception) {
        call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
    }
}

fun UnionChatsfromChats(chatList: List<Chat>): MutableList<UnionChat> {
    val listUnionChat = mutableListOf<UnionChat>()
    for (chat in chatList) {
        listUnionChat.add(UnionChat(chat))
    }
    return listUnionChat
}
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
            call.respond(Master.getUsers())
        }
        post("/register") {
            try {
                val creds = call.receive<UserPasswordCredential>()
                val id = Master.register(creds)
                call.respond(id)
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
                requestData<CreateLichkaRequest>({ params, principal ->
                    val sndUser = Master.findUserById(params.invitedId) ?: throw IllegalArgumentException()
                    val fstUser = Master.findUserByLogin(principal.name) ?: throw DoesNotExistException()
                    val id = Master.createLichka(fstUser, sndUser)
                    call.respondText("$id")
                }, call)
            }
            post("/createPublicChat") {
                requestData<CreatePublicChatRequest>({params, principal ->
                    val owner = Master.findUserByLogin(principal.name) ?: throw DoesNotExistException()
                    val chatId = Master.createPublicChat(owner, params.chatName)
                    call.respondText("PublicChatId: $chatId\n")
                }, call)
            }
            post("/inviteMember") {
                requestData<InviteMemberRequest>({params, principal ->
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    val invited = Master.findUserById(params.invitedId) ?: throw IllegalArgumentException()
                    val chat = Master.findChatById(params.chatId) ?: throw IllegalArgumentException()
                    Master.inviteUser(user, chat as PublicChat, invited)
                    call.respondText("OK")
                }, call)
            }
            post("/sendMessage") {
                requestData<SendMessageRequest>({params, principal ->
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    val chat = Master.findChatById(params.chatId) ?: throw IllegalArgumentException()
                    Master.sendMessage(user, chat, params.text)
                    call.respondText("OK")
                }, call)
            }
            post("/chats") {
                try {
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    val chats = Master.getUserChats(user.userID)
                    call.respond(UnionChatsfromChats(chats))
//                    call.respond(Master.getUserChats(user.userID))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
                }
            }
            post("/showMessages") {
                requestData<ShowMessageRequest>({params, principal ->
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    if (!Master.findChatMembers(params.chatId).contains(user)) {
                        throw DoesNotExistException("User not in the chat")
                    }
                    call.respond(Master.findChatMessages(params.chatId).map { it.text })
                }, call)
            }
            post("/deleteMessage") {
                requestData<DeleteMessageRequest>({params, principal ->
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    Master.deleteMessage(user, params.messageId)
                    call.respond("OK")
                }, call)
            }
            post("/contacts") {
                try {
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    call.respond(Master.getUserContacts(user.userID))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
                }
            }
            post("/changeContactName") {
                requestData<ChangeNameRequest>({params, principal ->
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    Master.changePreferredName(user.userID, params.userId, params.newName)
                    call.respondText("OK")
                }, call)
            }
            post("/blockUser") {
                requestData<BlockOrUnblockUserRequest>({params, principal ->
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException("User not found")
                    Master.blockUser(user.userID, params.userId)
                    call.respondText("OK")
                }, call)
            }
            post("/unblockUser") {
                requestData<BlockOrUnblockUserRequest>({params, principal ->
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException("User not found")
                    Master.unblockUser(user.userID, params.userId)
                    call.respondText("OK")
                }, call)
            }
            post("addContact") {
                requestData<AddOrDeleteContactRequest>({params, principal ->
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    Master.addContact(user.userID, params.userId)
                    call.respondText("OK")
                }, call)
            }
            post("deleteContact") {
                requestData<AddOrDeleteContactRequest>({params, principal ->
                    val user = Master.findUserByLogin(principal.name) ?: throw IllegalArgumentException()
                    Master.deleteContact(user.userID, params.userId)
                    call.respondText("OK")
                }, call)
            }
        }
    }
}