package ru.snailmail.backend

data class Message(val id : UID, val from: UID, var text: String) {
    var deleted = false
        private set
    private var edited = false

    fun edit(newText: String) {
        text = newText
        edited = true
    }

    fun delete() {
        deleted = true
    }

}