package ru.snailmail.backend

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.lang.IllegalArgumentException


abstract class Chat {
    val chatID = UIDGenerator.generateID()
    val messages = mutableListOf<Message>() // TODO: make private
    @JsonBackReference val members = mutableListOf<User>() // TODO: make private

    fun sendMessage(message: Message) = messages.add(message)

    /**
     * @param n number of last messages to print, -1 for all messages
     */
    fun printMessages (n: Int) {
        val nSafe = minOf(messages.size, if (n == -1) messages.size else n);
        for (i in (messages.size - nSafe) until messages.size) {
            println(messages[i])
        }
    }

    fun containsMessage(msg: Message) : Boolean {
        return messages.contains(msg)
    }

    fun deleteMessage(messageId: UID) {
        for (i in messages) {
            if (i.id == messageId) {
                i.delete()
            }
        }
    }
}

class PublicChat(var name : String, @JsonBackReference val owner : User) : Chat() {
    init {
        members.add(owner)
        owner.addChat(this)
    }

    fun addMember(member : User) {
        if (members.contains(member)) {
            throw AlreadyExistsException("$member is already in chat");
        }
        members.add(member)
        member.addChat(this)
    }

    fun changeName(new : String) {
        fun isCorrect(name : String) : Boolean = name.length in 1..64 && !name.contains("kek");
        if (!isCorrect(new)) {
            throw IllegalArgumentException("incorrect name")
        }
        name = new
    }
}


class Lichka(first : User, second: User) : Chat() {
    init {
        members.addAll(listOf(first, second))
        first.addChat(this)
        second.addChat(this)
    }
    @JsonIgnore
    fun getFirstUser(): User = members[0]
    @JsonIgnore
    fun getSecondUser(): User = members[1]
}