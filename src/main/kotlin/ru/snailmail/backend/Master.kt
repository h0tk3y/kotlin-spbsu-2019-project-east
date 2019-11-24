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
        Data.init()
    }
    val server = embeddedServer(Netty, port = 8080) {
        module()
    }
    server.start(wait = true)
}

object Master {
    fun clear() = Data.clear()

    fun register(creds: UserPasswordCredential): UID {
        if (creds.name == "") {
            throw IllegalArgumentException("Empty login")
        }
        val id = UIDGenerator.generateID()
        transaction {
            if (Data.findUserIdByLogin(creds.name) != null) {
                throw AlreadyExistsException("User with login ${creds.name} already exists")
            }
            Data.addUser(id.id, creds.name, creds.password)
        }
        return id
    }

    fun findChatById(id: UID): Chat? = Data.findChatById(id)

    fun findMessageById(id: UID): Message? = Data.findMessageById(id)

    fun findUserById(id: UID): User? = transaction { Data.findUserById(id) }

    fun findUserByLogin(userLogin: String): User? = Data.findUserByLogin(userLogin)

    fun logIn(creds: UserPasswordCredential): UID =
        transaction {
            var uid: UID? = null
            val id = Data.findUserIdByLogin(creds.name) ?: throw java.lang.IllegalArgumentException("Wrong login")
            if (Data.findUserById(id)!!.password != creds.password) {
                throw IllegalArgumentException("Wrong password")
            }
            id
        }

    fun sendMessage(user: User, c: Chat, text: String): UID =
        transaction {
            if (!Data.userInChat(user.userID, c.chatID)) {
                throw DoesNotExistException("User not in the chat")
            }
            val id = UIDGenerator.generateID()
            if (c is Lichka) {
                val fstUsr = c.getFirstUser()
                val sndUsr = c.getSecondUser()
                Data.findContact(fstUsr.userID, sndUsr.userID)?.let {
                    if (it.isBlocked) {
                        throw IllegalAccessException("User is blocked")
                    }
                }
                Data.findContact(sndUsr.userID, fstUsr.userID)?.let {
                    if (it.isBlocked) {
                        throw IllegalAccessException("User is blocked")
                    }
                }
            }
            Data.addMessage(c.chatID, Message(id, user.userID, text))
            id
        }

    fun deleteMessage(user: User, c: Chat, messageId: UID) =
        transaction {
            Data.deleteMessage(messageId)
        }

    fun createLichka(user1: User, user2: User): UID {
        if (user1 == user2) {
            throw AlreadyInTheChatException("User is already in the chat")
        }
        return transaction {
            if (Data.findLichkaByMembers(user1.userID, user2.userID) != null) {
                throw AlreadyExistsException("You already have a chat")
            }
            val id = UIDGenerator.generateID()
            Data.addLichka(id, user1.userID, user2.userID)
            id
        }
    }

    fun createPublicChat(owner: User, name: String): UID {
        val chat = PublicChat(name, owner)
        return transaction {
            val id = UIDGenerator.generateID()
            Data.addPublicChat(id, name, owner.userID)
            id
        }
    }

    fun inviteUser(chatMember: User, c: PublicChat, newMember: User) {
        transaction {
            if (!Data.userInChat(chatMember.userID, c.chatID)) {
                throw DoesNotExistException("User not in the chat")
            }
            if (Data.userInChat(newMember.userID, c.chatID)) {
                throw AlreadyInTheChatException("User is already in the chat")
            }
            Data.addUserToChat(c.chatID, newMember.userID)
        }
    }
}