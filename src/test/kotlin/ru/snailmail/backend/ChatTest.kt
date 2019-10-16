package ru.snailmail.backend

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ChatTest {

    @Test
    fun testCreateChat() {
        val user1 = User("Bob", "111")
        val user2 = User("Alica", "111")
        val chat = Lichka(user1, user2)
        assertTrue(chat in user1.chats)
        assertTrue(chat in user2.chats)
    }

    @Test
    fun testSendMessage() {
        val user1 = User("Bob", "111")
        val user2 = User("Alica", "111")
        val chat = Lichka(user1, user2)
        val message = Message(UIDGenerator.generateID() ,user1.userID, "Ping")
        chat.sendMessage(message)
        assertTrue(message in chat.messages)
    }
}