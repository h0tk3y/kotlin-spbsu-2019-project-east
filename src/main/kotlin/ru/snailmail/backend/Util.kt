package ru.snailmail.backend

object UIDGenerator  {
    fun generateID() : UID {
        return UID(0)
    }
}

data class UID (val id : Int);
