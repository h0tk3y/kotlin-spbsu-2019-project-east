package ru.snailmail.backend

object UIDGenerator  {
    var lastID : Long = 0
    fun generateID() : UID {
        return UID(lastID++);
    }
}

data class UID (val id : Long);
