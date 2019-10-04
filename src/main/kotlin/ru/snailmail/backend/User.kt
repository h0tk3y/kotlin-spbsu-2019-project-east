package ru.snailmail.backend

private val AlreadyExists = IllegalArgumentException("Already exists")
private val DoesNotExist = IllegalArgumentException("Does not exist")

data class Contact(val userID: UID, var preferredName: String, var isBlocked: Boolean)

class User(initName: String) {
    var name: String = initName
        private set
    val userID = UIDGenerator.generateID()
    val chats = mutableListOf<Chat>()
    val contacts = mutableMapOf<UID, Contact>() // Contact by its ID

    fun changeName(newName: String) { name = newName }

    fun addChat(chat: Chat) {
        if (chats.contains(chat)) {
            throw AlreadyExists
        }
        chats.add(chat)
    }

    fun deleteChat(chat: Chat) {
        if (!chats.contains(chat)) {
            throw DoesNotExist
        }
        chats.remove(chat)
    }

    fun addContact(u: User) {
        if (contacts.contains(u.userID)) {
            throw AlreadyExists
        }
        contacts[u.userID] = Contact(u.userID, u.name, false)
    }

    fun deleteContact(u: User) {
        if (!contacts.contains(u.userID)) {
            throw DoesNotExist
        }
        contacts.remove(u.userID)
    }
}