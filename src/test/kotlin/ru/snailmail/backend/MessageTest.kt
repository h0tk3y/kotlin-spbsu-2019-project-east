package ru.snailmail.backend

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import ru.snailmail.backend.*
import java.lang.IllegalArgumentException

class MessageTest {
    @Test
    fun testDeletingMessage() {
        val user = User("James")
        val message = Message(UIDGenerator.generateID(), user.userID, "Hello, World!")
        message.delete()
        Assertions.assertTrue(message.deleted)
    }

    @Test
    fun testEditingMessage() {
        val user = User("James")
        val message = Message(UIDGenerator.generateID(), user.userID, "There's typo in this srting.")
        message.edit("There's no typo in this string.")
        Assertions.assertTrue(message.text == "There's no typo in this string.")
    }
}