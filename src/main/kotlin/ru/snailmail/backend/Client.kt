package ru.snailmail.backend

class Client {
    private var u: User? = null

    fun register(login: String, password: String) {
        Master.register(login, password)
    }

    fun logIn(login: String, password: String) {
        u = Master.logIn(login, password)
    }

    fun sendMessage(c: Chat, msg: Message) {
        if (u == null) {
            throw IllegalArgumentException("User not registered")
        }
        if (!u!!.chats.contains(c)) {
            throw IllegalArgumentException("Chat doesn't exist")
        }
        c.sendMessage(msg)
    }

    fun createChat(c: Chat) {
        if (u == null) {
            throw IllegalArgumentException("User not registered")
        }
        u?.addChat(c)
    }

    fun inviteUser(c: Chat, user: User) {
        if (u == null) {
            throw IllegalArgumentException("User not registered")
        }
        user.addChat(c)
    }
}