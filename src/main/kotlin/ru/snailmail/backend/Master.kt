package ru.snailmail.backend

import io.ktor.auth.UserPasswordCredential
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) {
    Database.connect(
        "jdbc:h2:~/course_2/kotlin/kotlin-spbsu-2019-project-east",
        driver = "org.h2.Driver")

    transaction {
        SchemaUtils.create(Users)
    }

    val server = embeddedServer(Netty, port = 8080) {
        module()
    }
    server.start(wait = true)
}

object Users : Table() {
    val userId = integer("id").primaryKey()
    val name = varchar("name", length = 50)
    val password = integer("password")
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

    fun findUserById(id: UID): User? {
        return users.find { it.userID == id }
    }

    fun findChatById(id: UID): Chat? {
        return chats.find { it.chatID == id }
    }

    fun findMessageById(c: Chat, id: UID): Message? {
        return c.messages.find { it.id == id }
    }

    fun sendMessage(user: User, c: Chat, text: String): UID {
        if (!c.members.contains(user)) {
            throw DoesNotExistException("User not in the chat")
        }
        val id = UIDGenerator.generateID()
        if (c is Lichka) {
            c.first.contacts[c.second.userID]?.let {
                if (it.isBlocked) {
                    throw IllegalAccessException("User is blocked")
                }
            }
            c.second.contacts[c.first.userID]?.let {
                if (it.isBlocked) {
                    throw IllegalAccessException("User is blocked")
                }
            }
        }
        c.sendMessage(Message(id, user.userID, text))
        return id
    }

    fun deleteMessage(user: User, c: Chat, messageId: UID) {
        val message = findMessageById(c, messageId)
        message?.let { if (message.from == user.userID) c.deleteMessage(messageId) }
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

    fun inviteUser(chatMember: User, c: PublicChat, newmember: User) {
        if (!chatMember.chats.contains(c)) {
            throw DoesNotExistException("User not in the chat")
        }
        if (newmember.chats.contains(c)) {
            throw AlreadyInTheChatException("User is already in the chat")
        }
        c.addMember(newmember)
    }
}