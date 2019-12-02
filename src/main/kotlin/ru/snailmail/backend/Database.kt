package ru.snailmail.backend

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Date

object Data {
    object Users : Table() {
        val userId = long("id").primaryKey()
        val name = varchar("name", length = 50).primaryKey()
        val passwordHash = integer("passwordHash")
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
        val from = long("from")
        val text = varchar("text", length=100)
        val deleted = bool("deleted")
        val edited = bool("edited")
        val time = varchar("time", length=100)
        // TODO: add attachments.
    }

    object MessagesToChats : Table() {
        val chatId = long("chatId")
        val messageId = long("messageId")
    }
    // TODO: add references.
    // TODO: make Database visible only from Master.

    fun findUserByLogin(userLogin: String): User? {
        Users.select { Users.name eq userLogin }.firstOrNull()?.let {
            return User(it[Users.name], it[Users.passwordHash], UID(it[Users.userId]))
        }
        return null
    }

    fun addUser(id: Long, userLogin: String, userPasswordHash: Int) {
        Users.insert {
            it[userId] = id
            it[name] = userLogin
            it[passwordHash] = userPasswordHash
        }
    }

    fun findUserById(id: UID): User? {
        Users.select { Users.userId eq id.id }.singleOrNull()?.let {
            return User(it[Users.name], it[Users.passwordHash], id)
        }
        return null
    }

    fun userInChat(userId: UID, chatId: UID): Boolean =
        ChatsToUsers.select { (ChatsToUsers.chatId eq chatId.id) and (ChatsToUsers.userId eq userId.id) }.empty()

    fun findContact(fstId: UID, sndId: UID): Contact? {
        Contacts.select { (Contacts.ownerId eq fstId.id) and (Contacts.userId eq sndId.id) }.singleOrNull()?.let {
            return Contact(UID(it[Contacts.ownerId]), UID(it[Contacts.userId]),
                it[Contacts.preferredName], it[Contacts.isBlocked])
        }
        return null
    }

    fun addMessage(chId: UID, m: Message) {
        Messages.insert {
            it[id] = m.id.id
            it[text] = m.text
            it[from] = m.from.id
            it[deleted] = m.deleted
            it[edited] = m.edited
            it[time] = m.time.toString()
        }
        MessagesToChats.insert {
            it[chatId] = chId.id
            it[messageId] = m.id.id
        }
    }

    fun addLichka(chId: UID, userId1: UID, userId2: UID) {
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

    fun addPublicChat(chId: UID, chName: String, ownerId: UID) {
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

    fun addUserToChat(chId: UID, uId: UID) {
        ChatsToUsers.insert {
            it[chatId] = chId.id
            it[userId] = uId.id
        }
    }

    fun addContact(ownId: UID, otherId: UID) {
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
        Users.selectAll().map { User(it[Users.name], it[Users.passwordHash], UID(it[Users.userId])) }

    fun findMessageById(id: UID): Message? =
        Messages.select { Messages.id eq id.id }.singleOrNull()?.let {
            Message(UID(it[Messages.id]), UID(it[Messages.from]), it[Messages.text], it[Messages.deleted], it[Messages.edited], Date(it[Messages.time]))
        }

    fun getUserChats(uId: UID): List<Chat> =
        ChatsToUsers.select { ChatsToUsers.userId eq uId.id }.map {
            findChatById(UID(it[ChatsToUsers.chatId]))
                ?: throw DatabaseInternalException("Illegal chatId in ChatsToUsers")
        }

    fun findChatMembers(chId: UID): List<User> =
        ChatsToUsers.select { ChatsToUsers.chatId eq chId.id }.map {
            findUserById(UID(it[ChatsToUsers.userId]))
                ?: throw DatabaseInternalException("Illegal userId in ChatsToUsers")
        }

    fun findChatMessages(chId: UID): List<Message> =
        MessagesToChats.select { MessagesToChats.chatId eq chId.id }.map {
            findMessageById(UID(it[MessagesToChats.messageId]))
                ?: throw DatabaseInternalException("Illegal messageId in MessagesToChats")
        }

    fun findLichkaByMembers(userId1: UID, userId2: UID): UID? =
        Lichkas.select {
            ((Lichkas.fstId eq userId1.id) and (Lichkas.sndId eq userId2.id)) or
                    ((Lichkas.fstId eq userId2.id) and (Lichkas.sndId eq userId1.id))
        }.singleOrNull()?.let { UID(it[Lichkas.id]) }

    fun findChatById(id: UID): Chat? =
        Lichkas.select { (Lichkas.id eq id.id) }.singleOrNull()?.let {
            val fst = findUserById(UID(it[Lichkas.fstId])) ?: return null
            val snd = findUserById(UID(it[Lichkas.sndId])) ?: return null
            Lichka(fst, snd)
        } ?: PublicChats.select { (PublicChats.id eq id.id) }.singleOrNull()?.let {
            val owner = findUserById(UID(it[PublicChats.owner])) ?: return null
            PublicChat(it[PublicChats.name], owner)
        }

    fun deleteMessage(msgId: UID): Boolean =
        Messages.update({ Messages.id eq msgId.id }) {
            it[deleted] = true
            it[text] = ""
        } > 0

    fun deleteContact(ownerId: UID, otherId: UID): Boolean =
        Contacts.deleteWhere { (Contacts.ownerId eq ownerId.id) and (Contacts.userId eq otherId.id) } > 0

    fun changePreferredName(ownerId: UID, otherId: UID, newName: String): Boolean =
        Contacts.update({ (Contacts.ownerId eq ownerId.id) and (Contacts.userId eq otherId.id) }) {
            it[preferredName] = newName
        } > 0

    fun blockUser(ownerId: UID, otherId: UID): Boolean =
        Contacts.update({ (Contacts.ownerId eq ownerId.id) and (Contacts.userId eq otherId.id) }) {
            it[isBlocked] = true
        } > 0

    fun unblockUser(ownerId: UID, otherId: UID): Boolean =
        Contacts.update({ (Contacts.ownerId eq ownerId.id) and (Contacts.userId eq otherId.id) }) {
            it[isBlocked] = false
        } > 0

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