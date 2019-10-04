package ru.snailmail.backend

open class Chat (var name : String) {
    val chatID = UIDGenerator.generateID()
    val messages = emptyList<Message>()
    val users = emptyList<User>()
    open fun sendMessage(message: Message) { }
    fun changeName (newName : String) {
        name = newName
    }
    open fun searchMessage(s : String) { }
    open fun printMessage (message : Message) { }

}