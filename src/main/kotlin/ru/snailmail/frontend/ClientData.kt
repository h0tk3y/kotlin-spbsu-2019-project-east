package ru.snailmail.frontend

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import ru.snailmail.backend.Message
import ru.snailmail.backend.UID
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

fun dateFromString(date : String) : Date {
    val timeInstant = ZonedDateTime.parse(date,
        DateTimeFormatter.ofPattern( "E MMM d HH:mm:ss z uuuu" )).toInstant()
    return Date.from(timeInstant)
}

object ClientData {
    object Messages : Table() {
        val id = long("id").primaryKey()
        val from = long("from")
        val text = varchar("text", length=100)
        val deleted = bool("deleted")
        val edited = bool("edited")
        val time = varchar("time", length=100)
        // TODO: add attachments.
    }

    object Chats : Table() {
        val id = long("id").primaryKey()
    }

    object MessagesToChats : Table() {
        val chatId = reference("chatId", Chats.id)
        val messageId = reference("messageId", Messages.id)
    }

    fun addMessage(chId: UID, m: Message) {
        if (Messages.select { Messages.id eq m.id.id }.empty()) {
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
    }

    fun addChat(uid : UID) {
        if (Chats.select { Chats.id eq uid.id }.empty()) {
            Chats.insert {
                it[id] = uid.id
            }
        }
    }

    fun findMessageById(id: UID): Message? =
        Messages.select { Messages.id eq id.id }.singleOrNull()?.let {
            Message(
                UID(it[Messages.id]), UID(it[Messages.from]), it[Messages.text], it[Messages.deleted],
                it[Messages.edited], dateFromString(it[Messages.time])
            )
        }


    fun init() {
        SchemaUtils.create(Messages)
        SchemaUtils.create(Chats)
        SchemaUtils.create(MessagesToChats)
    }

    fun clear() {
        transaction {
            MessagesToChats.deleteAll()
//            SchemaUtils.drop(Chats, MessagesToChats, Messages)
            Chats.deleteAll()
            Messages.deleteAll()
        }
    }
}