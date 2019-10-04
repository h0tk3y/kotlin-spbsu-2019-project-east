package ru.snailmail.backend

class User (var name : String) {
    val userID = UIDGenerator.generateID()
    val chatList = emptyList<Chat>()
    val userList = emptyList<User>() // saved

    fun changeName(newName : String) {
        name = newName
    }
}