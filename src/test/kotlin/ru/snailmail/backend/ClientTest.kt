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
        val chat = Lichka(client1.u, client2.u)
        client1.createChat(chat)
        Assertions.assertEquals(client1.u.chats, mutableListOf(chat))
        Assertions.assertEquals(client2.u.chats, mutableListOf(chat))
    }
}