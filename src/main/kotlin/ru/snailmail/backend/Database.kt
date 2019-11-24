package ru.snailmail.backend

import org.jetbrains.exposed.sql.*

object Users : Table() {
    val userId = long("id").primaryKey()
    val name = varchar("name", length = 50).primaryKey()
    val password = varchar("password", length = 50)
}

object Contacts : Table() {
    val ownerId = long("ownerId").primaryKey()
    val userId = long("userId")
    val prefferedName = varchar("prefferedName", length = 50)
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

fun findUserIdByLogin(t: Transaction, userLogin: String): UID? {
    Users.select { Users.name eq userLogin }.firstOrNull()?.let {
        return UID(it[Users.userId])
    }
    return null
}

fun addUser(t: Transaction, id: Long, userLogin: String, userPassword: String) {
    Users.insert {
        it[userId] = id
        it[name] = userLogin
        it[password] = userPassword
    }
}

fun findUserById(t: Transaction, id: UID): User? {
    Users.select { Users.userId eq id.id }.firstOrNull()?.let {
        return User(it[Users.name], it[Users.password], id)
    }
    return null
}

fun userInChat(t: Transaction, userId: UID, chatId: UID): Boolean =
    ChatsToUsers.select { (ChatsToUsers.chatId eq chatId.id) and (ChatsToUsers.userId eq userId.id) }.empty()
