package ru.snailmail.backend


import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import java.lang.IllegalArgumentException
import kotlin.Exception

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

    val login1 = "kekos"
    val login2 = "memos"
    val login3 = "abrikos"
    val password1 = "ogurets98"
    val password2 = "qwerty123"
    val password3 = "password"
    val text1 = "hello, memos"
    val text2 = "hello, kekos"
    val text3 = "hello, abrikos"

    private fun createClient(login: String, password: String): Client {
        val client = Client()
        client.register(login, password)
        client.logIn(login, password)
        return client
    }

    @Test
    fun testPrivateChat() {
        Master.clear()
        val client1 = createClient(login1, password1)
        val client2 = createClient(login2, password2)

        client1.createLichka(client2.u)
        val chat = client1.u.chats[0]

        client1.sendMessage(chat, text1)
        client2.sendMessage(chat, text2)
        
        Assertions.assertEquals(client1.u.chats, mutableListOf(chat))
        Assertions.assertEquals(client2.u.chats, mutableListOf(chat))
        Assertions.assertEquals(chat.messages.map { it.text }, mutableListOf(text1, text2))
    }

    @Test
    fun testPublicChat() {
        Master.clear()
        val client1 = createClient(login1, password1)
        val client2 = createClient(login2, password2)
        val client3 = createClient(login3, password3)

        client1.createPublicChat("public chat")
        val chat = client1.u.chats[0] as PublicChat
        client1.inviteUser(chat, client2.u)
        client1.inviteUser(chat, client3.u)

        client1.sendMessage(chat, text1)
        client2.sendMessage(chat, text2)
        client3.sendMessage(chat, text3)

        Assertions.assertEquals(client1.u.chats, mutableListOf(chat))
        Assertions.assertEquals(client2.u.chats, mutableListOf(chat))
        Assertions.assertEquals(client3.u.chats, mutableListOf(chat))
        Assertions.assertEquals(chat.messages.map { it.text }, mutableListOf(text1, text2, text3))
    }
}