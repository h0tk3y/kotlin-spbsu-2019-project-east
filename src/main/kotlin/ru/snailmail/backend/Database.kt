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
        val ownerId = long("ownerId")
        val userId = long("userId")
        val preferredName = varchar("preferredName", length = 50)
        val isBlocked = bool("isBlocked")
    }

    object Lichkas : Table() {
        val id = long("id").primaryKey()
        val fstId = long("fstId")
        val sndId = long("sndId")
    }

    object PublicChats : Table() {
        val id = long("id").primaryKey()
        val name = varchar("name", length = 50)
        val owner = long("owner")
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
    // TODO: add references

    fun findUserIdByLogin(userLogin: String): UID? {
        Users.select { Users.name eq userLogin }.firstOrNull()?.let {
            return UID(it[Users.userId])
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
        Users.select { Users.userId eq id.id }.singleOrNull()?.let {
            return User(it[Users.name], it[Users.password], id)
        }
        return null
    }

    fun userInChat(userId: UID, chatId: UID): Boolean =
        ChatsToUsers.select { (ChatsToUsers.chatId eq chatId.id) and (ChatsToUsers.userId eq userId.id) }.empty()

    fun findUserByLogin(userLogin: String): User? {
        Users.select { Users.name eq userLogin }.singleOrNull()?.let {
            return User(it[Users.name], it[Users.password], UID(it[Users.userId]))
        }
        return null
    }

    fun findContact(fstId: UID, sndId: UID): Contact? {
        Contacts.select { (Contacts.ownerId eq fstId.id) and (Contacts.userId eq sndId.id) }.singleOrNull()?.let {
            return Contact(UID(it[Contacts.ownerId]), UID(it[Contacts.userId]),
                it[Contacts.preferredName], it[Contacts.isBlocked])
        }
        return null
    }

    fun addMessage(chId: UID, m: Message): Unit {
        Messages.insert {
            it[id] = m.id.id
            it[deleted] = m.deleted
            it[edited] = m.edited
        }
        MessagesToChats.insert {
            it[chatId] = chId.id
            it[messageId] = m.id.id
        }
    }

    fun addLichka(chId: UID, userId1: UID, userId2: UID): Unit {
        Lichkas.insert {
            it[id] = chId.id
        }
        ChatsToUsers.insert {
            it[chatId] = chId.id
            it[userId] = userId1.id
        }
        ChatsToUsers.insert {
            it[chatId] = chId.id
            it[userId] = userId2.id
        }
    }

    fun addPublicChat(chId: UID, chName: String, ownerId: UID): Unit {
        PublicChats.insert {
            it[id] = chId.id
            it[name] = chName
            it[owner] = ownerId.id
        }
        ChatsToUsers.insert {
            it[chatId] = chId.id
            it[userId] = ownerId.id
        }
    }

    fun addUserToChat(chId: UID, uId: UID): Unit {
        ChatsToUsers.insert {
            it[chatId] = chId.id
            it[userId] = uId.id
        }
    }

    fun addContact(ownId: UID, otherId: UID): Unit {
        Contacts.insert {
            it[ownerId] = ownId.id
            it[userId] = otherId.id
            it[isBlocked] = false
        }
    }

    fun getUserContacts(uId: UID): List<Contact> =
        Contacts.select { Contacts.ownerId eq uId.id }.map {
            Contact(UID(it[Contacts.ownerId]), UID(it[Contacts.userId]),
                it[Contacts.preferredName], it[Contacts.isBlocked])
        }

    fun getUsers(): List<User> =
        Users.selectAll().map { User(it[Users.name], it[Users.password], UID(it[Users.userId])) }

    fun findChatById(id: UID): Chat? = TODO()

    fun findMessageById(id: UID): Message? = TODO()

    fun findChatMembers(chatId: UID): List<User> = TODO()

    fun findChatMessages(chatId: UID): List<Message> = TODO()

    fun getUserChats(userId: UID): List<Chat> = TODO()

    fun deleteMessage(msgId: UID): Unit = TODO()

    fun findLichkaByMembers(userId1: UID, userId2: UID): UID? = TODO()

    fun deleteContact(userId: UID, otherId: UID): Unit = TODO()

    fun changePreferredName(userId: UID, otherId: UID, newName: String): Boolean = TODO()

    fun blockUser(userId: UID, otherId: UID): Boolean = TODO()

    fun unblockUser(userId: UID, otherId: UID): Boolean = TODO()

    fun init() {
        SchemaUtils.create(Users)
        SchemaUtils.create(Contacts)
        SchemaUtils.create(Lichkas)
        SchemaUtils.create(PublicChats)
        SchemaUtils.create(ChatsToUsers)
        SchemaUtils.create(Messages)
        SchemaUtils.create(MessagesToChats)
    }

    fun clear() {
        transaction {
            Users.deleteAll()
            Contacts.deleteAll()
            Lichkas.deleteAll()
            PublicChats.deleteAll()
            ChatsToUsers.deleteAll()
            Messages.deleteAll()
            MessagesToChats.deleteAll()
        }
    }
}