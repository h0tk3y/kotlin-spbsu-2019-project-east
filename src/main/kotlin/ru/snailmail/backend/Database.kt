package ru.snailmail.backend

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Data {
    object Users : Table() {
        val userId = long("id").primaryKey()
        val name = varchar("name", length = 50).primaryKey()
        val password = varchar("password", length = 50)
    }

    object Contacts : Table() {
        val ownerId = long("ownerId").primaryKey()
        val userId = long("userId")
        val preferredName = varchar("preferredName", length = 50)
        val isBlocked = bool("isBlocked")
    }

    object Chats : Table() {
        val id = long("id").primaryKey()
        val isLichka = bool("isLichka")
    }

    object ChatsToUsers : Table() {
        val userId = long("userId")
        val chatId = long("chatId")
    }

    object Messages : Table() {
        val id = long("id").primaryKey()
        val deleted = bool("deleted")
        val edited = bool("edited")
        // TODO: add attachments
    }

    object MessagesToChats : Table() {
        val chatId = long("chatId")
        val messageId = long("messageId")
    }

    fun findUserByLogin(userLogin: String): User? {
        Users.select { Users.name eq userLogin }.firstOrNull()?.let {
            return User(it[Users.name], it[Users.password], UID(it[Users.userId]))
        }
        return null
    }

    fun addUser(id: Long, userLogin: String, userPassword: String) {
        Users.insert {
            it[userId] = id
            it[name] = userLogin
            it[password] = userPassword
        }
    }

    fun findUserById(id: UID): User? {
        Users.select { Users.userId eq id.id }.firstOrNull()?.let {
            return User(it[Users.name], it[Users.password], id)
        }
        return null
    }

    fun userInChat(userId: UID, chatId: UID): Boolean =
        ChatsToUsers.select { (ChatsToUsers.chatId eq chatId.id) and (ChatsToUsers.userId eq userId.id) }.empty()

    fun findContact(fstId: UID, sndId: UID): Contact? = TODO()

    fun addMessage(chatId: UID, m: Message): Unit = TODO()

    fun deleteMessage(msgId: UID): Unit = TODO()

    fun findLichkaByMembers(userId1: UID, userId2: UID): UID? = TODO()

    fun addLichka(chatId: UID, userId1: UID, userId2: UID): Unit = TODO()

    fun addPublicChat(chatId: UID, name: String, ownerId: UID): Unit = TODO()

    fun addUserToChat(chatId: UID, userId: UID): Unit = TODO()

    fun addContact(userId: UID, otherId: UID): Unit = TODO()

    fun deleteContact(userId: UID, otherId: UID): Unit = TODO()

    fun changePreferredName(userId: UID, otherId: UID, newName: String): Boolean = TODO()

    fun blockUser(userId: UID, otherId: UID): Boolean = TODO()

    fun unblockUser(userId: UID, otherId: UID): Boolean = TODO()

    fun getUserContacts(userId: UID): List<Contact> = TODO()

    fun findChatById(id: UID): Chat? = TODO()

    fun findMessageById(id: UID): Message? = TODO()

    fun findChatMembers(chatId: UID): List<User> = TODO()

    fun findChatMessages(chatId: UID): List<Message> = TODO()

    fun getUsers(): List<User> = TODO()

    fun getUserChats(userId: UID): List<Chat> = TODO()

    fun init() {
        SchemaUtils.create(Users)
        SchemaUtils.create(Contacts)
        SchemaUtils.create(Chats)
        SchemaUtils.create(ChatsToUsers)
        SchemaUtils.create(Messages)
        SchemaUtils.create(MessagesToChats)
    }

    fun clear() {
        transaction {
            Users.deleteAll()
            Contacts.deleteAll()
            Chats.deleteAll()
            ChatsToUsers.deleteAll()
            Messages.deleteAll()
            MessagesToChats.deleteAll()
        }
    }
}