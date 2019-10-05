package ru.snailmail.backend

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import ru.snailmail.backend.*
import java.lang.IllegalArgumentException

class MasterTest {
    @Test
    fun testAddingSameUser() {
        Master.addUser("a")
        Master.addUser("b")
        var failed = false
        Assertions.assertThrows(AlreadyExistsException::class.java) {Master.addUser("a")};
    }
}