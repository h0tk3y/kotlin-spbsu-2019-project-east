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

    fun register(creds: UserPasswordCredential): UID {
        // Consider limiting the login/password length?
        if (creds.name == "") {
            throw IllegalArgumentException("Empty login")
        }
        if (users.any { it.name == creds.name }) {
            throw AlreadyExistsException("User with login ${creds.name} already exists")
        }
        val user = User(creds.name, creds.password)
        users.add(user)
        return user.userID
    }


    fun logIn(creds: UserPasswordCredential): User {
        val user = users.find { it.name == creds.name } ?: throw DoesNotExistException("Wrong login")
        if (user.password != creds.password) {
            throw IllegalArgumentException("Wrong password")
        }
        return user
    }

    fun findUserByLogin(userLogin: String): User? {
        return users.find { it.name == userLogin }
    }

    fun findUserById(id: Int): User? {
        return users.find { it.userID.id == id.toLong() }
    }

    fun findChatById(id: Int): Chat? {
        return chats.find { it.chatID.id == id.toLong() }
    }

    fun sendMessage(user: User, c: Chat, text: String): UID {
        val id = UIDGenerator.generateID()
        c.sendMessage(Message(id, user.userID, text))
        return id
    }

    fun deleteMessage(c: Chat, messageId: UID) {
        c.deleteMessage(messageId)
    }

    fun createLichka(user1: User, user2: User): UID {
        if (user1 == user2) {
            throw AlreadyInTheChatException("User is already in the chat")
        }
        if (user1.chats.filterIsInstance<Lichka>().any { it.members.contains(user2) }) {
            throw AlreadyExistsException("You already have a chat")
        }
        val lichka = Lichka(user1, user2)
        chats.add(lichka)
        return lichka.chatID
    }

    fun createPublicChat(owner: User, name: String): UID {
        val chat = PublicChat(name, owner)
        chats.add(chat)
        return chat.chatID
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