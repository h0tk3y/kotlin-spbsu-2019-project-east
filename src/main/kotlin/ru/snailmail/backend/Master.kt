package ru.snailmail.backend

fun main() {
    val login = "boryan"
    val password = "11111111"
    Master.register(login, password)
}

object Master {
    private val users = mutableListOf<User>()
    private val chats = mutableListOf<Chat>()

    fun clear() {
        users.clear()
        chats.clear()
    }

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

    fun createLichka(user1: User, user2: User) {
        // TODO: throw exception when exists.
        chats.add(Lichka(user1, user2))
    }

    fun createPublicChat(owner: User, name: String) {
        // TODO: throw exception when exists.
        chats.add(PublicChat(name, owner))
    }

    fun inviteUser(chatmember: User, c: PublicChat, newmember: User) {
        // TODO: throw exception when newmember is already in c or chatmember is not a member of c.
        c.addMember(newmember)
    }
}