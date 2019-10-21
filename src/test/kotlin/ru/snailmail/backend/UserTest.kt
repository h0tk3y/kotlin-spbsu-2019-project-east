package ru.snailmail.backend

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserTest {
    @Test
    fun testChangeName() {
        val user = User("Alan", "111")
        user.changeName("Bob")
        assertTrue(user.name == "Bob")
    }

    @Test
    fun testAddChat() {
        val user = User("Chris", "111")
        val chat = PublicChat("First Chat", user)
        assertEquals(user.chats, mutableListOf(chat))
        assertThrows(AlreadyExistsException::class.java) { user.addChat(chat) }
    }

    @Test
    fun testDeleteChat() {
        val user = User("Chris", "111")
        val chat = PublicChat("First Chat", user)
        assertDoesNotThrow { user.deleteChat(chat) }
        assertTrue(user.chats.isEmpty())
    }

    @Test
    fun testAddContact() {
        val user1 = User("kekos", "111")
        val user2 = User("memos", "222")
        assertDoesNotThrow { user1.addContact(user2) }
        assertEquals(user1.contacts,
            mutableMapOf(user2.userID to Contact(user2.userID, user2.name, false)))
        assertThrows(AlreadyExistsException::class.java) { user1.addContact(user2) }
    }

    @Test
    fun testDeleteContact() {
        val user1 = User("kekos", "111")
        val user2 = User("memos", "222")
        user1.addContact(user2)
        assertDoesNotThrow { user1.deleteContact(user2) }
        assertEquals(user1.contacts, emptyMap<UID, Contact>())
        assertThrows(DoesNotExistException::class.java) { user1.deleteContact(user2) }
    }
}