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
        val ownerId = reference("ownerId", Users.userId)
        val userId = reference("userId", Users.userId)
        val preferredName = varchar("preferredName", length = 50)
        val isBlocked = bool("isBlocked")
    }

    object Chats : Table() {
        val id = long("id").primaryKey()
    }

    object Lichkas : Table() {
        val chatId = reference("id", Chats.id).primaryKey()
        val fstId = reference("fstId", Users.userId)
        val sndId = reference("sndId", Users.userId)
    }

    object PublicChats : Table() {
        val chatId = reference("id", Chats.id).primaryKey()
        val name = varchar("name", length = 50)
        val owner = reference("owner", Users.userId)
    }

    object ChatsToUsers : Table() {
        val userId = reference("userId", Users.userId)
        val chatId = reference("chatId", Chats.id)
    }

    object Messages : Table() {
        val id = long("id").primaryKey()
        val from = reference("from", Users.userId)
        val text = varchar("text", length=100)
        val deleted = bool("deleted")
        val edited = bool("edited")
        val time = varchar("time", length=100)
        // TODO: add attachments.
    }

    object MessagesToChats : Table() {
        val chatId = reference("chatId", Chats.id)
        val messageId = reference("messageId", Messages.id)
    }
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
        ChatsToUsers.select { (ChatsToUsers.chatId eq chatId.id) and (ChatsToUsers.userId eq userId.id) }.any()

    fun findContact(fstId: UID, sndId: UID): Contact? =
        Contacts.select { (Contacts.ownerId eq fstId.id) and (Contacts.userId eq sndId.id) }.singleOrNull()?.let {
            Contact(UID(it[Contacts.ownerId]), UID(it[Contacts.userId]),
                it[Contacts.preferredName], it[Contacts.isBlocked])
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
        Chats.insert {
            it[id] = chId.id
        }
        Lichkas.insert {
            it[chatId] = chId.id
            it[fstId] = userId1.id
            it[sndId] = userId2.id
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
        Chats.insert {
            it[id] = chId.id
        }
        PublicChats.insert {
            it[chatId] = chId.id
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
            Message(UID(it[Messages.id]), UID(it[Messages.from]), it[Messages.text], it[Messages.deleted],
                it[Messages.edited], Date(it[Messages.time]))
        }

    fun getUserChats(uId: UID): List<Chat> =
        PublicChats.join(ChatsToUsers, JoinType.INNER, null, null) {
            PublicChats.chatId eq ChatsToUsers.chatId
        }.select {
            ChatsToUsers.userId eq uId.id
        }.map { // TODO: fix this shit
                PublicChat(UID(it[PublicChats.chatId]), it[PublicChats.name], findUserById(UID(it[PublicChats.owner]))!!)
            }.plus(Lichkas.join(ChatsToUsers, JoinType.INNER, null, null) {
                Lichkas.chatId eq ChatsToUsers.chatId
            }.select {
                ChatsToUsers.userId eq uId.id
            }.map { // TODO: fix this shit
                Lichka(UID(it[Lichkas.chatId]), findUserById(UID(it[Lichkas.fstId]))!!, findUserById(UID(it[Lichkas.sndId]))!!)
            }
            ) // TODO: sort by time

    fun findChatMembers(chId: UID): List<User> =
        Users.join(ChatsToUsers, JoinType.INNER, null, null) {
            Users.userId eq ChatsToUsers.userId
        }.select {
            ChatsToUsers.chatId eq chId.id
        }.map { User(it[Users.name], it[Users.passwordHash], UID(it[Users.userId])) }

    fun findChatMessages(chId: UID): List<Message> =
        Messages.join(MessagesToChats, JoinType.INNER, null, null) {
            Messages.id eq MessagesToChats.messageId
        }.select {
            MessagesToChats.chatId eq chId.id
        }.map {
            Message(UID(it[Messages.id]), UID(it[Messages.from]), it[Messages.text], it[Messages.deleted],
                it[Messages.edited], Date(it[Messages.time]))
        }

    fun findLichkaByMembers(userId1: UID, userId2: UID): UID? =
        Lichkas.select {
            ((Lichkas.fstId eq userId1.id) and (Lichkas.sndId eq userId2.id)) or
                    ((Lichkas.fstId eq userId2.id) and (Lichkas.sndId eq userId1.id))
        }.singleOrNull()?.let { UID(it[Lichkas.chatId]) }

    fun findChatById(id: UID): Chat? =
        Lichkas.select { (Lichkas.chatId eq id.id) }.singleOrNull()?.let {
            val fst = findUserById(UID(it[Lichkas.fstId])) ?: return null
            val snd = findUserById(UID(it[Lichkas.sndId])) ?: return null
            Lichka(id, fst, snd)
        } ?: PublicChats.select { (PublicChats.chatId eq id.id) }.singleOrNull()?.let {
            val owner = findUserById(UID(it[PublicChats.owner])) ?: return null
            PublicChat(id, it[PublicChats.name], owner)
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
        SchemaUtils.create(Chats)
        SchemaUtils.create(Lichkas)
        SchemaUtils.create(PublicChats)
        SchemaUtils.create(ChatsToUsers)
        SchemaUtils.create(Messages)
        SchemaUtils.create(MessagesToChats)
    }

    fun clear() {
        transaction {
            Contacts.deleteAll()
            Lichkas.deleteAll()
            PublicChats.deleteAll()
            ChatsToUsers.deleteAll()
            MessagesToChats.deleteAll()
            Messages.deleteAll()
            Users.deleteAll()
            Chats.deleteAll()
        }
    }
}