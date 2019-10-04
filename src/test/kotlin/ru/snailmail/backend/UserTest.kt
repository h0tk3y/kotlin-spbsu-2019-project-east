package ru.snailmail.backend

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException

class UserTest {
    @Test
    fun testChangeName() {
        val user = User("Alan")
        user.changeName("Bob")
        Assertions.assertTrue(user.name == "Bob")
    }
    @Test
    fun testAddChat() {
        val user = User("Chris")
        val chat = Chat("First Chat")
        user.addChat(chat)
        Assertions.assertTrue(user.chatList[0] == chat)
        var failed = false
        try {
            user.addChat(chat)
        }  catch (e : IllegalArgumentException) {
            failed = true
        }
        Assertions.assertTrue(failed)

    }
}