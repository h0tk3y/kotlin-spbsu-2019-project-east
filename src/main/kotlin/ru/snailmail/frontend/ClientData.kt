package ru.snailmail.frontend

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import ru.snailmail.backend.Data
import ru.snailmail.backend.Message
import ru.snailmail.backend.UID
import java.util.Date

class ClientData {
    object Users : Table() {
        val userId = long("id").primaryKey()
        val name = varchar("name", length = 50).primaryKey()
        val passwordHash = integer("passwordHash")
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

    fun addMessage(chId: UID, m: Message) {
        Data.Messages.insert {
            it[id] = m.id.id
            it[text] = m.text
            it[from] = m.from.id
            it[deleted] = m.deleted
            it[edited] = m.edited
            it[time] = m.time.toString()
        }
        Data.MessagesToChats.insert {
            it[chatId] = chId.id
            it[messageId] = m.id.id
        }
    }

    fun findMessageById(id: UID): Message? =
        Data.Messages.select { Data.Messages.id eq id.id }.singleOrNull()?.let {
            Message(
                UID(it[Data.Messages.id]), UID(it[Data.Messages.from]), it[Data.Messages.text], it[Data.Messages.deleted],
                it[Data.Messages.edited], Date(it[Data.Messages.time])
            )
        }


    fun init() {
        SchemaUtils.create(Users)
        SchemaUtils.create(Messages)
    }

    fun clear() {
        transaction {
            Users.deleteAll()
        }
    }
}