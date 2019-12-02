package ru.snailmail.backend

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

import ru.snailmail.backend.*
import java.lang.IllegalArgumentException

class MessageTest {
    @Test
    fun testDeletingMessage() {
        val user = User("James", 111)
        val message = Message(UIDGenerator.generateID(), user.userID, "Hello, World!")
        message.delete()
        assertTrue(message.deleted)
    }

    @Test
    fun testEditingMessage() {
        val user = User("James", 111)
        val message = Message(UIDGenerator.generateID(), user.userID, "There's typo in this srting.")
        message.edit("There's no typo in this string.")
        assertTrue(message.text == "There's no typo in this string.")
    }
}