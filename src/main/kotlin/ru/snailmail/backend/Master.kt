package ru.snailmail.backend

import io.ktor.auth.UserPasswordCredential
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) {
    val connection = Database.connect(
        "jdbc:h2:./testdb",
        driver = "org.h2.Driver"
    )

    transaction(connection) {
        Data.clear()
        Data.init()
    }
    val server = embeddedServer(Netty, port = 8080) {
        module()
    }
    server.start(wait = true)
}

object Master {
    fun clear() = Data.clear()

    // TODO: make this function private but visible in tests?
    fun hash(s: String, userId: UID) = (userId.id.toString() + s).hashCode()

    fun register(creds: UserPasswordCredential): UID {
        if (creds.name == "") {
            throw IllegalArgumentException("Empty login")
        }
        return transaction {
            if (Data.findUserByLogin(creds.name) != null) {
                throw AlreadyExistsException("User with login ${creds.name} already exists")
            }
            val id = UIDGenerator.generateID()
            Data.addUser(id.id, creds.name, hash(creds.password, id))
            id
        }
    }

    fun logIn(creds: UserPasswordCredential): UID =
        transaction {
            val id = Data.findUserByLogin(creds.name)?.userID ?: throw IllegalArgumentException("Wrong login")
            if (Data.findUserById(id)!!.passwordHash != hash(creds.password, id)) {
                throw IllegalArgumentException("Wrong password")
            }
            id
        }

    fun sendMessage(user: User, c: Chat, text: String): UID =
        transaction {
            if (!Data.userInChat(user.userID, c.chatID)) {
                throw DoesNotExistException("User not in the chat")
            }
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
            val id = UIDGenerator.generateID()
            Data.addMessage(c.chatID, Message(id, user.userID, text))
            id
        }

    fun deleteMessage(user: User, messageId: UID) {
        transaction {
            val msg = Data.findMessageById(messageId)
                ?: throw IllegalArgumentException("No such message with given id")
            if (msg.from != user.userID)
                throw IllegalAccessException("Message does not belong to the specified user")
            if (msg.deleted)
                throw IllegalAccessException("Message is already deleted")
            Data.deleteMessage(messageId)
        }
    }

    fun editMessage(user: User, messageId: UID, text: String) {
        transaction {
            val msg = Data.findMessageById(messageId)
                ?: throw IllegalArgumentException("No such message with given id")
            if (msg.from != user.userID)
                throw IllegalAccessException("Message does not belong to the specified user")
            if (msg.deleted)
                throw IllegalAccessException("Message is deleted")
            Data.editMessage(messageId, text)
        }
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

    fun logOutOfChat(chatMember: User, chat: PublicChat) {
        transaction {
            if (!Data.userInChat(chatMember.userID, chat.chatId)) {
                throw IllegalAccessException("User not in the chat")
            }
            Data.deleteUserFromChat(chatMember.userID, chat.chatId)
        }
    }

    fun findChatById(id: UID): Chat? =
        transaction { Data.findChatById(id) }

    fun findPublicChatById(id: UID): PublicChat? =
        transaction { Data.findPublicChatById(id) }

    fun findMessageById(id: UID): Message? =
        transaction { Data.findMessageById(id) }

    fun findUserById(id: UID): User? =
        transaction { Data.findUserById(id) }

    fun findUserByLogin(userLogin: String): User? =
        transaction { Data.findUserByLogin(userLogin) }

    fun getUsers(): List<User> =
        transaction { Data.getUsers() }

    fun getUserChats(userId: UID): List<Chat> =
        transaction { Data.getUserChats(userId) }

    fun findChatMembers(chatId: UID): List<User> =
        transaction { Data.findChatMembers(chatId) }

    fun findChatMessages(chatId: UID): List<Message> =
        transaction { Data.findChatMessages(chatId) }

    fun getUserContacts(userId: UID): List<Contact> =
        transaction { Data.getUserContacts(userId) }

    fun changePreferredName(userId: UID, otherId: UID, newName: String) =
        transaction { Data.changePreferredName(userId, otherId, newName) }

    fun blockUser(userId: UID, otherId: UID): Boolean =
        transaction { Data.blockUser(userId, otherId) }

    fun unblockUser(userId: UID, otherId: UID): Boolean =
        transaction { Data.unblockUser(userId, otherId) }

    fun addContact(ownId: UID, otherId: UID) =
        transaction { Data.addContact(ownId, otherId) }

    fun deleteContact(userId: UID, otherId: UID): Unit =
        transaction { Data.deleteContact(userId, otherId) }
}