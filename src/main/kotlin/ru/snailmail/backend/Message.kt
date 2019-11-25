package ru.snailmail.backend

data class Message(val id : UID, val from: UID, var text: String, var deleted: Boolean, var edited: Boolean) {

    constructor(id: UID,  from: UID, text: String) :
            this(id, from, text, false, false)

    // TODO: add time
    private val attachments = mutableListOf<Attachment>()

    fun addAttachment(attachment: Attachment) {
        attachments.add(attachment)
    }

    fun edit(newText: String) {
        text = newText
        edited = true
    }

    fun delete() {
        deleted = true
    }

}