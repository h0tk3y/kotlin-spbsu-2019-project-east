package ru.snailmail.backend

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.lang.IllegalArgumentException


abstract class Chat(val chatID: UID) {
    constructor(): this(UIDGenerator.generateID())
}

data class PublicChat(var name : String, @JsonBackReference val owner : User) : Chat()

class Lichka(private val first : User, private val second: User) : Chat() {
    fun getFirstUser(): User = first
    fun getSecondUser(): User = second
}