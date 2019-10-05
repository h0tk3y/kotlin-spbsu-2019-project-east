package ru.snailmail.backend

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.Exception

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
        Assertions.assertDoesNotThrow { user.addChat(chat) }
        Assertions.assertEquals(user.chats, mutableListOf(chat))
        Assertions.assertThrows(AlreadyExistsException::class.java) { user.addChat(chat) }
    }

    @Test
    fun testDeleteChat() {
        val user = User("Chris")
        val chat = Chat("First Chat")
        Assertions.assertThrows(DoesNotExistException::class.java) { user.deleteChat(chat) }
        user.addChat(chat)
        Assertions.assertDoesNotThrow { user.deleteChat(chat) }
        Assertions.assertTrue(user.chats.isEmpty())
    }

    @Test
    fun testAddContact() {
        val user1 = User("kekos")
        val user2 = User("memos")
        Assertions.assertDoesNotThrow { user1.addContact(user2) }
        Assertions.assertEquals(user1.contacts,
            mutableMapOf(user2.userID to Contact(user2.userID, user2.name, false)))
        Assertions.assertThrows(AlreadyExistsException::class.java) { user1.addContact(user2) }
    }

    @Test
    fun testDeleteContact() {
        val user1 = User("kekos")
        val user2 = User("memos")
        user1.addContact(user2)
        Assertions.assertDoesNotThrow { user1.deleteContact(user2) }
        Assertions.assertEquals(user1.contacts, emptyMap<UID, Contact>())
        Assertions.assertThrows(DoesNotExistException::class.java) { user1.deleteContact(user2) }
    }
}