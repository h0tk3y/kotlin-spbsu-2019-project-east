package ru.snailmail.backend

import com.fasterxml.jackson.annotation.JsonManagedReference
import io.ktor.auth.Principal

data class Contact(val userID: UID, var preferredName: String, var isBlocked: Boolean)

class User(initName: String, initPassword: String, id: UID) : Principal {
    var name: String = initName
        private set
    val password: String = initPassword
    val userID = id

    constructor(initName: String, initPassword: String) : this(initName, initPassword, UIDGenerator.generateID())
}