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

    @Test
    fun testPrivateChat() {
        Master.clear()
        val client1 = Client()
        val client2 = Client()

        client1.register(login1, password1)
        client2.register(login2, password2)
        client1.logIn(login1, password1)
        client2.logIn(login2, password2)

        client1.createLichka(client2.u)
        Assertions.assertEquals(client1.u.chats.size, 1)
        Assertions.assertEquals(client2.u.chats.size, 1)

        client1.sendMessage(client1.u.chats[0], text1)
        client2.sendMessage(client2.u.chats[0], text2)
        Assertions.assertEquals(client1.u.chats, client2.u.chats)
        Assertions.assertEquals(client1.u.chats[0].messages.size, 2)
        Assertions.assertEquals(client1.u.chats[0].messages[0].text, text1)
        Assertions.assertEquals(client1.u.chats[0].messages[1].text, text2)
    }

    @Test
    fun testPublicChat() {
        Master.clear()
        val client1 = Client()
        val client2 = Client()
        val client3 = Client()
        client1.register(login1, password1)
        client2.register(login2, password2)
        client3.register(login3, password3)
        client1.logIn(login1, password1)
        client2.logIn(login2, password2)
        client3.logIn(login3, password3)

        client1.createPublicChat("public chat")
        client1.inviteUser(client1.u.chats[0] as PublicChat, client2.u)
        client1.inviteUser(client1.u.chats[0] as PublicChat, client3.u)
        Assertions.assertEquals(client1.u.chats.size, 1)
        Assertions.assertEquals(client2.u.chats.size, 1)
        Assertions.assertEquals(client3.u.chats.size, 1)
        client1.sendMessage(client1.u.chats[0], text1)
        client2.sendMessage(client1.u.chats[0], text2)
        client3.sendMessage(client1.u.chats[0], text3)
        Assertions.assertEquals(client1.u.chats, client2.u.chats)
        Assertions.assertEquals(client1.u.chats, client3.u.chats)
        Assertions.assertEquals(client1.u.chats[0].messages.size, 3)
        Assertions.assertEquals(client1.u.chats[0].messages[0].text, text1)
        Assertions.assertEquals(client1.u.chats[0].messages[1].text, text2)
        Assertions.assertEquals(client1.u.chats[0].messages[2].text, text3)
    }
}