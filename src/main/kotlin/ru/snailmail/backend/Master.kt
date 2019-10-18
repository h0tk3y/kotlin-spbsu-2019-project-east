package ru.snailmail.backend
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondText("west — lohi!", ContentType.Text.Plain)
            }
        }
    }
    server.start(wait = true)
}

object Master {
    private val users = mutableListOf<User>()
    private val chats = mutableListOf<Chat>()

    fun clear() {
        users.clear()
        chats.clear()
    }

    fun register(userLogin: String, userPassword: String) {
        // Consider limiting the login/password length?
        if (userLogin == "") {
            throw IllegalArgumentException("Empty login")
        }
        if (users.any { it.name == userLogin }) {
            throw AlreadyExistsException("User with login $userLogin already exists")
        }
        users.add(User(userLogin, userPassword))
    }


    fun logIn(userLogin: String, password: String): User {
        val user = users.find { it.name == userLogin } ?: throw DoesNotExistException("Wrong login")
        if (user.password != password) {
            throw IllegalArgumentException("Wrong password")
        }
        return user
    }

    fun searchUser(userLogin: String): User {
        return users.find { it.name == userLogin } ?: throw DoesNotExistException("$userLogin login doesn't exist")
    }

    fun sendMessage(user: User, c: Chat, text: String): UID {
        val id = UIDGenerator.generateID()
        c.sendMessage(Message(id, user.userID, text))
        return id
    }

    fun createLichka(user1: User, user2: User) {
        if (user1 == user2) {
            throw AlreadyInTheChatException()
        }
        if (user1.chats.filter { it is Lichka }.any { it.members.contains(user2) }) {
            throw AlreadyExistsException()
        }
        chats.add(Lichka(user1, user2))
    }

    fun createPublicChat(owner: User, name: String) {
        chats.add(PublicChat(name, owner))
    }

    fun inviteUser(chatmember: User, c: PublicChat, newmember: User) {
        if (!chatmember.chats.contains(c)) {
            throw DoesNotExistException("User not in the chat")
        }
        if (newmember.chats.contains(c)) {
            throw AlreadyInTheChatException()
        }
        c.addMember(newmember)
    }
}