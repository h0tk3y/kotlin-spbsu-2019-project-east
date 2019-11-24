package ru.snailmail.backend

import io.ktor.auth.UserPasswordCredential
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) {
    val connection = Database.connect(
        "jdbc:h2:./testdb",
        driver = "org.h2.Driver")

    transaction(connection) {
        SchemaUtils.create(Users)
    }
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
        if (creds.name == "") {
            throw IllegalArgumentException("Empty login")
        }
        val id = UIDGenerator.generateID()
        transaction {
            if (findUserIdByLogin(this, creds.name) != null) {
                throw AlreadyExistsException("User with login ${creds.name} already exists")
            }
            addUser(this, id.id, creds.name, creds.password)
        }
        return id
    }


    fun logIn(creds: UserPasswordCredential): UID =
        transaction {
            var uid: UID? = null
            val id = findUserIdByLogin(this, creds.name) ?: throw java.lang.IllegalArgumentException("Wrong login")
            if (findUserById(this, id)!!.password != creds.password) {
                throw IllegalArgumentException("Wrong password")
            }
            id
        }

    fun findUserByLogin(userLogin: String): User? = TODO()

    fun findUserById(id: UID): User? = transaction { findUserById(this, id) }

    fun findChatById(id: UID): Chat? = TODO()

    fun findMessageById(c: Chat, id: UID): Message? = TODO()

    fun sendMessage(user: User, c: Chat, text: String): UID {
        transaction {
            if (!userInChat(this, user.userID, c.chatID)) {
                throw DoesNotExistException("User not in the chat")
            }
            val id = UIDGenerator.generateID()
            if (c is Lichka) {
                val fstUsr = c.getFirstUser()
                val sndUsr = c.getSecondUser()
                fstUsr.contacts[sndUsr.userID]?.let {
                    if (it.isBlocked) {
                        throw IllegalAccessException("User is blocked")
                    }
                }
                sndUsr.contacts[fstUsr.userID]?.let {
                    if (it.isBlocked) {
                        throw IllegalAccessException("User is blocked")
                    }
                }
            }
            c.sendMessage(Message(id, user.userID, text))
            id
        }
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