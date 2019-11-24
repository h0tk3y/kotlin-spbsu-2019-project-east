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
            Users.selectAll().forEach {
                if (it[Users.name] == creds.name)
                    throw AlreadyExistsException("User with login ${creds.name} already exists")
            }
            Users.insert {
                it[name] = creds.name
                it[password] = creds.password
                it[userId] = id.id
            }
        }
        return id
    }


    fun logIn(creds: UserPasswordCredential): UID {
        return transaction {
            var uid: UID? = null
            Users.selectAll().forEach {
                if (it[Users.name] == creds.name) {
                    if (it[Users.password] != creds.password)
                        throw IllegalArgumentException("Wrong password")
                    uid = UID(it[Users.userId])
                }
            }
            uid
        } ?: throw DoesNotExistException("Wrong login")
    }

    fun findUserByLogin(userLogin: String): User? = transaction { findUserByLogin(this, userLogin) }

    fun findUserById(id: UID): User? = transaction { findUserById(this, id) }

    fun findChatById(id: UID): Chat? = transaction { findChatById(this, id) }

    fun findMessageById(c: Chat, id: UID) = transaction { findMessageById(this, c, id) }

    fun sendMessage(user: User, c: Chat, text: String): UID {
        if (!c.members.contains(user)) {
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