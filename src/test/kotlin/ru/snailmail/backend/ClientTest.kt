package ru.snailmail.backend


import io.ktor.auth.UserPasswordCredential
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import java.lang.IllegalArgumentException

class ClientTest {
    @BeforeEach
    private fun clear() = Master.clear()

    @Test
    fun testRegister() {
        val client = Client()
        assertDoesNotThrow {client.register(UserPasswordCredential("Grisha", "my password"))}
        assertDoesNotThrow {Master.findUser("Grisha")}
        assertTrue(Master.findUser("Grisha").name == "Grisha" &&
                Master.findUser("Grisha").password == "my password")
        assertThrows(IllegalArgumentException::class.java) {
            client.register(UserPasswordCredential("", "password"))
        }
    }

    @Test
    fun testLogIn() {
        val client = Client()
        assertThrows(DoesNotExistException::class.java) {
            client.logIn(UserPasswordCredential("Anton", "my password"))
        }
        client.register(UserPasswordCredential("Anton", "my password"))
        val exception = assertThrows(IllegalArgumentException::class.java) {
            client.logIn(UserPasswordCredential("Anton", "not my password"))
        }
        assertEquals(exception.message, "Wrong password")
    }

    @Test
    fun testCreateLichka() {
        val client = createClient("Sid", "pistol")
        assertThrows(AlreadyInTheChatException::class.java) { client.createLichka(client.user) }
        val useressa = User("Nancy", "gun")
        assertDoesNotThrow { client.createLichka(useressa) }
        assertThrows(AlreadyExistsException::class.java) { client.createLichka(useressa) }
    }

    @Test
    fun testCreatePublicChat() {
        val client = createClient("Cobain", "Rvanina")
        client.createPublicChat("chat")
        val chat = client.user.chats[0]
        assertDoesNotThrow { client.sendMessage(chat, "goodby world") }
    }

    @Test
    fun testSendMessage() {
        val client = createClient("login", "password")
        client.createPublicChat("chat")
        val chat = client.user.chats[0]
        val id = client.sendMessage(chat, text1)
        assertEquals(client.user.chats[0].messages, mutableListOf(Message(id, client.user.userID, text1)))
    }

    @Test
    fun testInviteUser() {
        val client = createClient("Mayakovskiy", "Horosho!")
        Master.createPublicChat(client.user, "Brodyachaya Sobaka")
        val chat = client.user.chats[0] as PublicChat
        assertThrows(AlreadyInTheChatException::class.java) { client.inviteUser(chat, client.user) }
        val user = User("Lilya", "Glass of water")
        assertDoesNotThrow { client.inviteUser(chat, user) }
        assertTrue(user.chats.contains(chat))
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

    private fun createClient(name: String, password: String): Client {
        val client = Client()
        client.register(UserPasswordCredential(name, password))
        client.logIn(UserPasswordCredential(name, password))
        return client
    }

    @Test
    fun testPrivateChat() {
        val client1 = createClient(login1, password1)
        val client2 = createClient(login2, password2)

        client1.createLichka(client2.user)
        val chat = client1.user.chats[0]

        client1.sendMessage(chat, text1)
        client2.sendMessage(chat, text2)
        
        assertEquals(client1.user.chats, mutableListOf(chat))
        assertEquals(client2.user.chats, mutableListOf(chat))
        assertEquals(chat.messages.map { it.text }, mutableListOf(text1, text2))
    }

    @Test
    fun testPublicChat() {
        val client1 = createClient(login1, password1)
        val client2 = createClient(login2, password2)
        val client3 = createClient(login3, password3)

        client1.createPublicChat("public chat")
        val chat = client1.user.chats[0] as PublicChat
        assertThrows(DoesNotExistException::class.java) { client2.inviteUser(chat, client3.user) }
        client1.inviteUser(chat, client2.user)
        client1.inviteUser(chat, client3.user)
        assertThrows(AlreadyInTheChatException::class.java) { client1.inviteUser(chat, client2.user) }

        client1.sendMessage(chat, text1)
        client2.sendMessage(chat, text2)
        client3.sendMessage(chat, text3)

        assertEquals(client1.user.chats, mutableListOf(chat))
        assertEquals(client2.user.chats, mutableListOf(chat))
        assertEquals(client3.user.chats, mutableListOf(chat))
        assertEquals(chat.messages.map { it.text }, mutableListOf(text1, text2, text3))
    }
}