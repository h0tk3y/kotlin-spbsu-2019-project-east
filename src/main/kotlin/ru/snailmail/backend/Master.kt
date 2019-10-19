package ru.snailmail.backend
import io.ktor.auth.UserPasswordCredential
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, port = 8080) {
        module()
    }
    server.start(wait = true)
}

object Master {
    val users = mutableListOf<User>()
    private val chats = mutableListOf<Chat>()

    fun clear() {
        users.clear()
        chats.clear()
    }

    fun register(creds: UserPasswordCredential) {
        // Consider limiting the login/password length?
        if (creds.name == "") {
            throw IllegalArgumentException("Empty login")
        }
        if (users.any { it.name == creds.name }) {
            throw AlreadyExistsException("User with login ${creds.name} already exists")
        }
        users.add(User(creds.name, creds.password))
    }


    fun logIn(creds: UserPasswordCredential): User {
        val user = users.find { it.name == creds.name } ?: throw DoesNotExistException("Wrong login")
        if (user.password != creds.password) {
            throw IllegalArgumentException("Wrong password")
        }
        return user
    }

    fun searchUser(userLogin: String): User {
        return users.find { it.name == userLogin } ?: throw DoesNotExistException("$userLogin login doesn't exist")
    }

    fun searchUserById(id: Int): User? {
        return users.find { it.userID.id.toInt() == id }
    }

    fun sendMessage(user: User, c: Chat, text: String): UID {
        val id = UIDGenerator.generateID()
        c.sendMessage(Message(id, user.userID, text))
        return id
    }

    fun createLichka(user1: User, user2: User) {
        if (user1 == user2) {
            throw AlreadyInTheChatException("User is already in the chat")
        }
        if (user1.chats.filterIsInstance<Lichka>().any { it.members.contains(user2) }) {
            throw AlreadyExistsException("You already have a chat")
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
            throw AlreadyInTheChatException("User is already in the chat")
        }
        c.addMember(newmember)
    }
}