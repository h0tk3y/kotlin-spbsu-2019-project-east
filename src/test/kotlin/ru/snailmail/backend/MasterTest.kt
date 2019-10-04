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
        try {
            Master.addUser("a")
        } catch (e : IllegalArgumentException) {
            failed = true
        }
        Assertions.assertTrue(failed)
    }
}