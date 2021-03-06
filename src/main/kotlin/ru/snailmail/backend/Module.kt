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

interface Request

data class CreateLichkaRequest(val invitedLogin: String) : Request

data class SendMessageRequest(val chatId: UID, val text: String) : Request

data class DeleteMessageRequest(val messageId: UID) : Request

data class EditMessageRequest(val messageId: UID, val text: String) : Request

data class ChatRequest(val chatId: UID) : Request

data class SearchInMessagesRequest(val userId: UID, val substring: String) : Request

data class CreatePublicChatRequest(val chatName: String) : Request

data class InviteMemberRequest(val chatId: UID, val invitedId: UID) : Request

data class ChangeNameRequest(val userId: UID, val newName: String) : Request

data class BlockOrUnblockUserRequest(val userId: UID) : Request

data class AddOrDeleteContactRequest(val userId: UID) : Request

suspend inline fun <reified T : Any> requestData(f: (T, UserIdPrincipal) -> Unit, call: ApplicationCall) {
    try {
        val params = call.receive<T>()
        val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
        f(params, principal)
    } catch (e: Exception) {
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
                val user = Master.findUserById(uid)
                val token = JwtConfig.makeToken(uid, creds)
                call.respond("$token$${objectMapper.writeValueAsString(user)}")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
            }
        }
        authenticate {
            post("/createLichka") {
                requestData<CreateLichkaRequest>({ params, principal ->
                    val sndUser = Master.findUserByLogin(params.invitedLogin)
                        ?: throw IllegalArgumentException("No user ${params.invitedLogin}")
                    val fstUser = Master.findUserByLogin(principal.name)
                        ?: throw DoesNotExistException("No user ${principal.name}")
                    val id = Master.createLichka(fstUser, sndUser)
                    call.respond(id)
                }, call)
            }
            post("/createPublicChat") {
                requestData<CreatePublicChatRequest>({ params, principal ->
                    val owner = Master.findUserByLogin(principal.name)
                        ?: throw DoesNotExistException("No user ${principal.name}")
                    val chatId = Master.createPublicChat(owner, params.chatName)
                    call.respond(chatId)
                }, call)
            }
            post("/inviteMember") {
                requestData<InviteMemberRequest>({ params, principal ->
                    val user = Master.findUserByLogin(principal.name)
                        ?: throw DoesNotExistException("No user ${principal.name}")
                    val invited = Master.findUserById(params.invitedId)
                        ?: throw DoesNotExistException("No user ${params.invitedId}")
                    val chat = Master.findChatById(params.chatId)
                        ?: throw DoesNotExistException("No chat ${params.chatId}")
                    Master.inviteUser(user, chat as PublicChat, invited)
                    call.respondText("OK")
                }, call)
            }
            post("/sendMessage") {
                requestData<SendMessageRequest>({ params, principal ->
                    val user = Master.findUserByLogin(principal.name)
                        ?: throw DoesNotExistException("No user ${principal.name}")
                    val chat = Master.findChatById(params.chatId)
                        ?: throw DoesNotExistException("No chat ${params.chatId}")
                    Master.sendMessage(user, chat, params.text)
                    call.respondText("OK")
                }, call)
            }
            post("/chats") {
                try {
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name)
                        ?: throw DoesNotExistException("No user ${principal.name}")
                    val chats = Master.getUserChats(user.userID)
                    call.respond(UnionChatsfromChats(chats))
                   } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
                }
            }
            post("/showMessages") {
                requestData<ChatRequest>({ params, principal ->
                    val user = Master.findUserByLogin(principal.name)
                        ?: throw DoesNotExistException("No user ${principal.name}")
                    if (!Master.findChatMembers(params.chatId).map { it.userID.id }.contains(user.userID.id)) {
                        throw DoesNotExistException("User ${principal.name} not in the chat ${params.chatId}")
                    }
                    call.respond(Master.findChatMessages(params.chatId))
                }, call)
            }
            post("/deleteMessage") {
                requestData<DeleteMessageRequest>({ params, principal ->
                    val user = Master.findUserByLogin(principal.name)
                        ?: throw DoesNotExistException("No user ${principal.name}")
                    Master.deleteMessage(user, params.messageId)
                    call.respond("OK")
                }, call)
            }
            post("/editMessage") {
                requestData<EditMessageRequest>({ params, principal ->
                    val user = Master.findUserByLogin(principal.name)
                        ?: throw DoesNotExistException("No user ${principal.name}")
                    Master.editMessage(user, params.messageId, params.text)
                    call.respond("OK")
                }, call)
            }
            post("/logOutOfChat") {
                requestData<ChatRequest>({ params, principal ->
                    val user = Master.findUserByLogin(principal.name)
                        ?: throw DoesNotExistException("No user ${principal.name}")
                    val chat = Master.findPublicChatById(params.chatId)
                        ?: throw DoesNotExistException("No chat ${params.chatId}")
                    Master.logOutOfChat(user, chat)
                }, call)
            }
            post("/contacts") {
                try {
                    val principal = call.principal<UserIdPrincipal>() ?: error("No Principal")
                    val user = Master.findUserByLogin(principal.name)
                        ?: throw DoesNotExistException("No user ${principal.name}")
                    call.respond(Master.getUserContacts(user.userID))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "error: ".plus(e.message))
                }
            }
            post("/changeContactName") {
                requestData<ChangeNameRequest>({ params, principal ->
                    val user = Master.findUserByLogin(principal.name)
                        ?: throw DoesNotExistException("No user ${principal.name}")
                    Master.changePreferredName(user.userID, params.userId, params.newName)
                    call.respondText("OK")
                }, call)
            }
            post("/blockUser") {
                requestData<BlockOrUnblockUserRequest>({ params, principal ->
                    val user = Master.findUserByLogin(principal.name)
                        ?: throw DoesNotExistException("No user ${principal.name}")
                    Master.blockUser(user.userID, params.userId)
                    call.respondText("OK")
                }, call)
            }
            post("/unblockUser") {
                requestData<BlockOrUnblockUserRequest>({ params, principal ->
                    val user = Master.findUserByLogin(principal.name)
                        ?: throw DoesNotExistException("No user ${principal.name}")
                    Master.unblockUser(user.userID, params.userId)
                    call.respondText("OK")
                }, call)
            }
            post("/searchInMessages") {
                requestData<SearchInMessagesRequest>({ params, principal ->
                    val user = Master.findUserByLogin(principal.name)
                        ?: throw DoesNotExistException("No user ${principal.name}")
                    call.respond(Master.searchInMessages(user, params.substring))
                }, call)
            }
            post("/addContact") {
                requestData<AddOrDeleteContactRequest>({ params, principal ->
                    val user = Master.findUserByLogin(principal.name)
                        ?: throw DoesNotExistException("No user ${principal.name}")
                    Master.addContact(user.userID, params.userId)
                    call.respondText("OK")
                }, call)
            }
            post("/deleteContact") {
                requestData<AddOrDeleteContactRequest>({ params, principal ->
                    val user = Master.findUserByLogin(principal.name)
                        ?: throw DoesNotExistException("No user ${principal.name}")
                    Master.deleteContact(user.userID, params.userId)
                    call.respondText("OK")
                }, call)
            }
        }
    }
}