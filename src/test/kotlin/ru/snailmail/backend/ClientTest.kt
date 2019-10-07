package ru.snailmail.backend

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.Exception

class ClientTest {
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