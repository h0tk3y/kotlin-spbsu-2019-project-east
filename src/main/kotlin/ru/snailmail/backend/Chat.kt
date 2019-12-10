package ru.snailmail.backend

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.lang.IllegalArgumentException


abstract class Chat(val chatID: UID) {
    constructor(): this(UIDGenerator.generateID())
}

data class PublicChat(val chatId: UID, var name: String, @JsonBackReference val owner: User) : Chat(chatId) {
    constructor(name: String, owner: User): this(UIDGenerator.generateID(), name, owner)
}

class Lichka(private val chatId: UID, private val first: User, private val second: User) : Chat(chatId) {
    constructor(first: User, second: User): this(UIDGenerator.generateID(), first, second)

    fun getFirstUser(): User = first
    fun getSecondUser(): User = second
}

data class UnionChat(var chatId: UID, var name: String) {
    constructor(chat: Chat) : this(chat.chatID, "lateinit") {
        if (chat is Lichka) {
            this.name = chat.getFirstUser().name + " with " + chat.getSecondUser().name
        }
        if (chat is PublicChat) {
            this.name = chat.name
        }
    }
}