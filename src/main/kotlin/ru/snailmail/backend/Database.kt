package ru.snailmail.backend

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction

object Users : Table() {
    val userId = long("id").primaryKey()
    val name = varchar("name", length = 50)
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
    val userId = long("userId").primaryKey()
    val chatId = long("chatId").primaryKey()
}

object Messages : Table() {
    val id = long("id").primaryKey()
    val deleted = bool("deleted")
    val edited = bool("edited")
    // TODO: add attachments
}

object MessagesToChats : Table() {
    val chatId = long("chatId").primaryKey()
    val messageId = long("messageId").primaryKey()
}

fun findUserIdByLogin(t: Transaction, userLogin: String): UID? = TODO()

fun addUser(t: Transaction, id: Long, login: String, password: String): Unit = TODO()

fun findUserById(t: Transaction, id: UID): User? = TODO()

fun userInChat(t: Transaction, userId: UID, chatId: UID): Boolean = TODO()