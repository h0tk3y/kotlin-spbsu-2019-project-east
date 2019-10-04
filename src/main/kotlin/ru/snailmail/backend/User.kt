package ru.snailmail.backend

class User(var name: String) {
    val userID = UIDGenerator.generateID()
    val chatList = mutableListOf<Chat>()
    val contactsList = mutableListOf<Contact>()

    fun changeName(newName : String) { name = newName }

    inner class Contact {
        val userID: Int = 0 // Id of the corresponding user
        val prefferedName: String = name
        val isBlocked: Boolean = false
    }

    fun addChat(chat: Chat) {
        if (chatList.contains(chat)) {
            throw IllegalArgumentException("Already exists")
        }
        chatList.add(chat)
    }

    fun deleteChat(chat: Chat) {
        if (!chatList.contains(chat)) {
            throw IllegalArgumentException("chat doesn't exist")
        }
        chatList.remove(chat)
    }

    fun AddContact(u: User): Nothing = TODO()
    fun SetPrefferedName(id: Int, newPrefferedName: String): Nothing = TODO()
    fun BlockUser(id: Int): Nothing = TODO()
    fun UnblockUser(id: Int): Nothing = TODO()
}