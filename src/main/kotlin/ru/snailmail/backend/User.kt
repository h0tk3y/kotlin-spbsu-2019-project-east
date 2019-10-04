package ru.snailmail.backend

class User (var name : String) {
    val userID = UIDGenerator.generateID()
    val chatList = mutableListOf<Chat>()
    val userList = mutableListOf<User>() // saved

    fun changeName(newName : String) {
        name = newName
    }

    fun addChat (chat: Chat) {
        if (chatList.contains(chat)) {
            throw IllegalArgumentException("Already exist")
        }
        chatList.add(chat)
    }

    fun deleteChat(chat: Chat) {
        if (!chatList.contains(chat)) {
            throw IllegalArgumentException("chat doesn't exist")
        }
        chatList.remove(chat)
    }
}