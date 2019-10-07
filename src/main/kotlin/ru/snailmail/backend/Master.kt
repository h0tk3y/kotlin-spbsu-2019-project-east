package ru.snailmail.backend

fun main() {
    val login = "boryan"
    val password = "11111111"
    Master.register(login, password)
}

object Master {
    private val users = mutableListOf<User>()
    private val chats = mutableListOf<Chat>()
    private var lastId = 0

    fun register(userLogin: String, userPassword: String) {
        for (user in users) {
            if (user.name == userLogin) {
                throw AlreadyExistsException("User with login $userLogin already exists")
            }
        }
        users.add(User(userLogin, userPassword))
    }

    fun logIn(userLogin: String, password: String): User {
        for (user in users) {
            if (user.name == userLogin) {
                if (user.password == password) { return user }
                throw IllegalArgumentException("Wrong password")
            }
        }
        throw DoesNotExistException("Wrong login")
    }

    fun searchUser(userLogin: String): User {
        for (user in users) {
            if (user.name == userLogin) {
                return user
            }
        }
        throw DoesNotExistException("User with login $userLogin already exists")
    }

}