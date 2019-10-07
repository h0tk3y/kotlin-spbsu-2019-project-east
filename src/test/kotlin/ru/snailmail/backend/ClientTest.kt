package ru.snailmail.backend

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import java.lang.IllegalArgumentException

class ClientTest {
    @Test
    fun testRegister() {
        val client = Client()
        Assertions.assertDoesNotThrow {client.register("Grisha", "my password")}
        Assertions.assertDoesNotThrow {Master.searchUser("Grisha")}
        Assertions.assertTrue(Master.searchUser("Grisha").name == "Grisha" &&
                Master.searchUser("Grisha").password == "my password")
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            client.register("", "password")
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            client.register("login", "")
        }
        // we need to set upper bound for login length
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            client.register("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                "password")
        }
        // we need to set upper bound for password length
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            client.register("name", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
        }
        //I think we want to have only eng letters
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            client.register("Вася", "password")
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            client.register("login", "границы ключ")
        }
    }

    @Test
    fun testLogIn() {
        val client = Client()
        Assertions.assertThrows(DoesNotExistException::class.java) {
            client.logIn("Anton", "my password")
        }
        client.register("Anton", "my password")
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            client.logIn("Anton", "not my password")
        }
        try {
            client.logIn("Anton", "not my password")
        } catch (e: IllegalArgumentException) {
            Assertions.assertEquals(e.message, "Wrong password")
        }
    }

    @Test
    fun testCreateChat() {
        val client = Client()
        val user = User("Sherlock", "violin")
        val chat = PublicChat("chat", user)
        Assertions.assertThrows(IllegalArgumentException::class.java) {client.createChat(chat)}
        client.register("Moriarty", "evil666")
        Assertions.assertThrows(IllegalArgumentException::class.java) {client.createChat(chat)}
        client.logIn("Moriarty", "evil666")
        Assertions.assertDoesNotThrow {client.createChat(chat)}
        Assertions.assertTrue(Master.searchUser("Moriarty").chats.contains(chat))
    }

    @Test
    fun testSendMessage() {
        val client = Client()
        val user = User("Sid", "pistol")
        val chat = PublicChat("chat", user)
        val msgid = UIDGenerator.generateID()
        val message = Message(msgid, user.userID, "goodby world")
        Assertions.assertThrows(IllegalArgumentException::class.java) {client.sendMessage(chat, message)}
        client.register("Sid", "pistol")
        Assertions.assertThrows(IllegalArgumentException::class.java) {client.sendMessage(chat, message)}
        client.logIn("Sid", "pistol")
        Assertions.assertThrows(IllegalArgumentException::class.java) {client.sendMessage(chat, message)}
        client.createChat(chat)
        Assertions.assertDoesNotThrow {client.sendMessage(chat, message)}
        Assertions.assertTrue(Master.searchUser("Sid").chats.find { it == chat }!!.containsMessage(message))
    }

    @Test
    fun testInviteUser() {
        val client = Client()
        val admin = User("Mayakovskiy", "Horosho!")
        val chat = PublicChat("Brodyachaya Sobaka", admin)
        Assertions.assertThrows(AlreadyInTheChatException::class.java) { client.inviteUser(chat, admin) }
        val user = User("Lilya", "Glass of water")
        Assertions.assertDoesNotThrow { client.inviteUser(chat, user) }
        Assertions.assertTrue(user.chats.contains(chat))
    }
}