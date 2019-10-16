package ru.snailmail.backend

data class Contact(val userID: UID, var preferredName: String, var isBlocked: Boolean)

class User(initName: String, initPassword: String) {
    var name: String = initName
        private set
    val password: String = initPassword
    val userID = UIDGenerator.generateID()
    val chats = mutableListOf<Chat>()
    val contacts = mutableMapOf<UID, Contact>() // Contact by its ID

    fun changeName(newName: String) { name = newName }

    fun addChat(chat: Chat) {
        if (chats.contains(chat)) {
            throw AlreadyExistsException()
        }
        chats.add(chat)
    }

    fun deleteChat(chat: Chat) {
        if (!chats.contains(chat)) {
            throw DoesNotExistException()
        }
        chats.remove(chat)
    }

    fun addContact(user: User) {
        if (contacts.contains(user.userID)) {
            throw AlreadyExistsException()
        }
        contacts[user.userID] = Contact(user.userID, user.name, false)
    }

    fun deleteContact(user: User) {
        if (!contacts.contains(user.userID)) {
            throw DoesNotExistException()
        }
        contacts.remove(user.userID)
    }
}