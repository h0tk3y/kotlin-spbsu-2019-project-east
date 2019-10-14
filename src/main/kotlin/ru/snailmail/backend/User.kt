package ru.snailmail.backend

data class Contact(val userID: UID, var preferredName: String, var isBlocked: Boolean)
data class UserData(var name: String, var password: String)

class User(initName: String, initPassword: String) {
    val data = UserData(initName, initPassword)
    val userID = UIDGenerator.generateID()
    val chats = mutableListOf<Chat>()
    val contacts = mutableMapOf<UID, Contact>() // Contact by its ID

    fun changeName(newName: String) { data.name = newName }

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

    fun addContact(u: User) {
        if (contacts.contains(u.userID)) {
            throw AlreadyExistsException()
        }
        contacts[u.userID] = Contact(u.userID, u.data.name, false)
    }

    fun deleteContact(u: User) {
        if (!contacts.contains(u.userID)) {
            throw DoesNotExistException()
        }
        contacts.remove(u.userID)
    }
}