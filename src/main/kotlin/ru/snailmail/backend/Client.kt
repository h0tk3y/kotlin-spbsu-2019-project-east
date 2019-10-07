package ru.snailmail.backend

class Client {
    lateinit var u: User
        private set

    fun register(login: String, password: String) {
        Master.register(login, password)
    }

    fun logIn(login: String, password: String) {
        u = Master.logIn(login, password)
    }

    fun sendMessage(c: Chat, text: String) {
        if (!u.chats.contains(c)) {
            throw IllegalArgumentException("Chat doesn't exist")
        }
        c.sendMessage(Message(UIDGenerator.generateID(), u.userID, text))
    }

    fun createLichka(user: User) {
        Master.createLichka(u, user)
    }

    fun inviteUser(c: Chat, user: User) {
        user.addChat(c)
    }
}