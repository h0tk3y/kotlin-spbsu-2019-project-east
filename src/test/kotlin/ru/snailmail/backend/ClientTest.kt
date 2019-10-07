package ru.snailmail.backend

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.Exception

class ClientTest {
    @Test
    fun testPrivateChat() {
        val client1 = Client()
        val client2 = Client()
        client1.register("kekos", "566")
        client2.register("memos", "239")
        client1.logIn("kekos", "566")
        client2.logIn("memos", "239")
        client1.createLichka(client2.u)
        Assertions.assertEquals(client1.u.chats.size, 1)
        Assertions.assertEquals(client2.u.chats.size, 1)
        val text1 = "hello, memos"
        val text2 = "hello, kekos"
        client1.sendMessage(client1.u.chats[0], text1)
        client2.sendMessage(client1.u.chats[0], text2)
        Assertions.assertEquals(client1.u.chats[0].messages.size, 2)
        Assertions.assertEquals(client1.u.chats[0].messages[0].text, text1)
        Assertions.assertEquals(client1.u.chats[0].messages[1].text, text2)
    }
}