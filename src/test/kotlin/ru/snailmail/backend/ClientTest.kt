package ru.snailmail.backend


import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions
import java.lang.IllegalArgumentException

class ClientTest {
    @BeforeEach
    private fun clear() = Master.clear()

    @Test
    fun testRegister() {
        val client = Client()
        Assertions.assertDoesNotThrow {client.register("Grisha", "my password")}
        Assertions.assertDoesNotThrow {Master.searchUser("Grisha")}
        Assertions.assertTrue(Master.searchUser("Grisha").data.name == "Grisha" &&
                Master.searchUser("Grisha").data.password == "my password")
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            client.register("", "password")
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
    fun testCreateLichka() {
        val client = createClient("Sid", "pistol")
        Assertions.assertThrows(AlreadyInTheChatException::class.java) { client.createLichka(client.u) }
        val user_essa = User("Nancy", "gun")
        Assertions.assertDoesNotThrow { client.createLichka(user_essa) }
        Assertions.assertThrows(AlreadyExistsException::class.java) { client.createLichka(user_essa) }
    }

    @Test
    fun testCreatePublicChat() {
        val client = createClient("Cobain", "Rvanina")
        client.createPublicChat("chat")
        val chat = client.u.chats[0]
        Assertions.assertDoesNotThrow { client.sendMessage(chat, "goodby world") }
    }

    @Test
    fun testSendMessage() {
        val client = createClient("login", "password")
        client.createPublicChat("chat")
        val chat = client.u.chats[0]
        val id = client.sendMessage(chat, text1)
        Assertions.assertEquals(client.u.chats[0].messages, mutableListOf(Message(id, client.u.userID, text1)))
    }

    @Test
    fun testInviteUser() {
        val client = createClient("Mayakovskiy", "Horosho!")
        Master.createPublicChat(client.u, "Brodyachaya Sobaka")
        val chat = client.u.chats[0] as PublicChat
        Assertions.assertThrows(AlreadyInTheChatException::class.java) { client.inviteUser(chat, client.u) }
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
        val client1 = createClient(login1, password1)
        val client2 = createClient(login2, password2)
        val client3 = createClient(login3, password3)

        client1.createPublicChat("public chat")
        val chat = client1.u.chats[0] as PublicChat
        Assertions.assertThrows(DoesNotExistException::class.java) { client2.inviteUser(chat, client3.u) }
        client1.inviteUser(chat, client2.u)
        client1.inviteUser(chat, client3.u)
        Assertions.assertThrows(AlreadyInTheChatException::class.java) { client1.inviteUser(chat, client2.u) }

        client1.sendMessage(chat, text1)
        client2.sendMessage(chat, text2)
        client3.sendMessage(chat, text3)

        Assertions.assertEquals(client1.u.chats, mutableListOf(chat))
        Assertions.assertEquals(client2.u.chats, mutableListOf(chat))
        Assertions.assertEquals(client3.u.chats, mutableListOf(chat))
        Assertions.assertEquals(chat.messages.map { it.text }, mutableListOf(text1, text2, text3))
    }
}