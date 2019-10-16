package ru.snailmail.backend

class Client {
    // TODO: request Master using network
    lateinit var user: User
        private set

    fun register(login: String, password: String) {
        Master.register(login, password)
    }

    fun logIn(login: String, password: String) {
        user = Master.logIn(login, password)
    }

    fun sendMessage(c: Chat, text: String): UID {
        if (!::user.isInitialized) {
            throw IllegalAccessException("Not registered")
        }
        if (!user.chats.contains(c)) {
            throw IllegalArgumentException("Chat doesn't exist")
        }
        return Master.sendMessage(user, c, text)
    }

    fun createLichka(user: User) {
        Master.createLichka(this.user, user)
    }

    fun createPublicChat(name: String) {
        if (!::user.isInitialized) {
            throw IllegalAccessException("Not registered")
        }
        Master.createPublicChat(user, name)
    }

    fun inviteUser(c: PublicChat, user: User) {
        Master.inviteUser(this.user, c, user)
    }
}