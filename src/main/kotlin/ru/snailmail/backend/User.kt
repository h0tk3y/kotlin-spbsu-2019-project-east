package ru.snailmail.backend

import io.ktor.auth.Principal

data class Contact(val ownerID: UID, var userId: UID, var preferredName: String, var isBlocked: Boolean)

class User(name: String, val passwordHash: Int, val userID: UID) : Principal {
    var name: String = name
        private set

    constructor(initName: String, initPasswordHash: Int) : this(initName, initPasswordHash, UIDGenerator.generateID())
}