package ru.snailmail.backend

import com.fasterxml.jackson.annotation.JsonBackReference


abstract class Chat(val chatID: UID) {
    constructor(): this(UIDGenerator.generateID())
}

data class PublicChat(val chatId: UID, var name : String, @JsonBackReference val owner : User) : Chat(chatId) {
    constructor(name : String, owner : User): this(UIDGenerator.generateID(), name, owner)
}

class Lichka(private val chatId: UID, private val first : User, private val second: User) : Chat(chatId) {
    constructor(first : User, second: User): this(UIDGenerator.generateID(), first, second)

    fun getFirstUser(): User = first
    fun getSecondUser(): User = second
}