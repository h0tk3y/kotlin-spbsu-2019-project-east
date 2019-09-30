package ru.snailmail.backend

open class Chat (var name : String, val chatId : Int) {
    var messages : List<Message> = emptyList()
    var users : List<User> = emptyList()
    open fun sendMessage(message: Message) { }
    fun changeName (newName : String) {
        name = newName
    }
    open fun searchMessage(s : String) { }
    open fun printMessage (message : Message) {}

}