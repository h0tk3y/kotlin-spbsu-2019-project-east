package ru.snailmail.backend

import io.ktor.auth.UserPasswordCredential

class Client {
    // TODO: request Master using network
    lateinit var user: User
        private set

    fun register(creds: UserPasswordCredential) {
        Master.register(creds)
    }

    fun logIn(creds: UserPasswordCredential) {
        user = Master.logIn(creds)
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