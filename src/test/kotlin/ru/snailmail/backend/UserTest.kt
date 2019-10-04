package ru.snailmail.backend

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException

class UserTest {
    @Test
    fun testChangeName() {
        val user = User("Alan")
        user.changeName("Bob")
        Assertions.assertTrue(user.name == "Bob")
    }
}