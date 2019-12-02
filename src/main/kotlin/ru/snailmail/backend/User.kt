package ru.snailmail.backend

import com.fasterxml.jackson.annotation.JsonManagedReference
import io.ktor.auth.Principal

data class Contact(val ownerID: UID, var userId: UID, var preferredName: String, var isBlocked: Boolean)

class User(initName: String, initPasswordHash: Int, id: UID) : Principal {
    var name: String = initName
        private set
    val passwordHash: Int = initPasswordHash
    val userID = id

    constructor(initName: String, initPasswordHash: Int) : this(initName, initPasswordHash, UIDGenerator.generateID())
}