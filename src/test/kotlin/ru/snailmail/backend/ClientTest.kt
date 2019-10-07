package ru.snailmail.backend

import org.junit.jupiter.api.Test

class ClientTest {
    @Test
    fun testRegister() {
        Client.register("Anton", "best password")
        Master.getUsers
    }
}