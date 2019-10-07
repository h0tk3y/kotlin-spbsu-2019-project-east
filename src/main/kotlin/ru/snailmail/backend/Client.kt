package ru.snailmail.backend

class Client {
    // TODO: request Master using network
    lateinit var u: User
        private set

    fun register(login: String, password: String) {
        Master.register(login, password)
    }

    fun logIn(login: String, password: String) {
        u = Master.logIn(login, password)
    }

    fun sendMessage(c: Chat, text: String) {
        if (!::u.isInitialized) {
            throw IllegalAccessException("Not registered")
        }
        if (!u.chats.contains(c)) {
            throw IllegalArgumentException("Chat doesn't exist")
        }
        c.sendMessage(Message(UIDGenerator.generateID(), u.userID, text))
    }

    fun createLichka(user: User) {
        if (!::u.isInitialized) {
            throw IllegalAccessException("Not registered")
        }
        Master.createLichka(u, user)
    }

    fun createPublicChat(name: String) {
        if (!::u.isInitialized) {
            throw IllegalAccessException("Not registered")
        }
        Master.createPublicChat(u, name)
    }

    fun inviteUser(c: PublicChat, user: User) {
        if (!::u.isInitialized) {
            throw IllegalAccessException("Not registered")
        }
        Master.inviteUser(u, c, user)
    }
}