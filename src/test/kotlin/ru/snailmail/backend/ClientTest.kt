package ru.snailmail.backend

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import java.lang.IllegalArgumentException

class ClientTest {
    @Test
    fun testRegister() {
        val client = Client()
        Assertions.assertDoesNotThrow {client.register("Grisha", "my password")}
        Assertions.assertTrue(Master.searchUser("Grisha").name == "Grisha" &&
                Master.searchUser("Grisha").password == "my password")
    }

    @Test
    fun testLogin() {
        val client = Client()
        client.register("Anton", "my password")
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            client.logIn("Anton", "not my password")}
        try {
            client.logIn("Anton", "not my password")
        } catch (e: IllegalArgumentException) {
            Assertions.assertEquals(e.message, "Wrong password")
        }
    }

    @Test
    fun testSendMessage() {
        //TODO
    }
}