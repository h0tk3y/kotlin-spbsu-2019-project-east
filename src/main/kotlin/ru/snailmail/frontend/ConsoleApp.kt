package ru.snailmail.frontend

import io.ktor.auth.UserPasswordCredential
import ru.snailmail.backend.Contact
import ru.snailmail.backend.UID
import kotlin.IllegalArgumentException

fun main() {
    val ca = ConsoleApp()
    ca.runner()
}

class ConsoleApp {
    private val client = Client()
    fun runner() {

        var flag = true
        println(client.greetings())

        while (flag) {
            val answer = readLine()
            when (answer) {
                "-h" -> help()
                "login" -> {
                    login()
                }
                "register" -> {
                    register()
                }
                "users" -> {
                    getUsers()
                }
                "create lichka" -> {
                    createLichka()
                }
                "create chat" -> {
                    createPublicChat()
                }
                "invite member" -> {
                    inviteMember()
                }
                "send message" -> {
                    sendMessage()
                }
                "get chats" -> {
                    getChats()
                }
                "get contacts" -> {
                    getContacts()
                }
                "add contact" -> {
                    addContact()
                }
                "exit" -> {
                    flag = false
                }
                else -> dumb()
            }
        }
    }

    private fun login() {
        println("Enter your name: ")
        val name = readLine()
        println("Enter your password: ")
        val pass = readLine()
        try {
            println(client.logIn(UserPasswordCredential(name ?: "", pass ?: "")))
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    private fun register() {
        println("Enter your name: ")
        val name = readLine()
        println("Enter your password: ")
        val pass = readLine()
        try {
            println(client.register(UserPasswordCredential(name ?: "", pass ?: "")))
            println("User $name signed up!")
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    private fun help() {
        print(
            "Possible commands:\n" +
                    "\tlogin          Залогиниться\n" +
                    "\tregister       Зарегистрироваться\n" +
                    "\tcreate lichka   Создать диалог\n" +
                    "\tcreate chat    Создать беседу\n" +
                    "\tinvite member Пригласить в беседу\n" +
                    "\tget contacts   Посмотреть контакты\n" +
                    "\tadd contact    Добавить контакт\n"
        )
    }

    private fun getUsers() {
        val users = client.getUsers()
        if (users.isEmpty()) println("No users")
        for (user in users) {
            println(user.name + ", " + "ID=${user.userID.id}")
        }
    }

    private fun createLichka() {
        println("Enter your friend's login:")
        val name = readLine()
        try {
            client.createLichka(name ?: "")
            println("Lichka with $name created")
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    private fun sendMessage() {
        println("Enter chat id:")
        val chatId = readLine()
        println("Enter message:")
        val text = readLine()
        try {
            println(client.sendMessage(UID(chatId?.toLong() ?: 0), text ?: ""))
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    private fun createPublicChat() {
        println("Enter chat name:")
        val chatName = readLine()
        try {
            client.createPublicChat(chatName ?: "")
            println("Chat $chatName created!")
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    private fun inviteMember() {
        println("Enter chat ID:")
        val chatID = readLine()
        println("Enter user ID:")
        val userID = readLine()
        try {
            println(client.inviteUser(UID(chatID?.toLong() ?: 0), UID(userID?.toLong() ?: 0)))
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    private fun getChats() {
        println(client.getChats())
    }

    private fun getContacts() {
        val contacts: List<Contact>
        try {
            contacts = client.getContacts()
            if (contacts.isEmpty()) {
                println("You haven't got friends")
                return
            }
            for (contact in contacts) {
                println(contact.preferredName)
            }
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    private fun addContact() {
        println("Enter contact's id:")
        val userID = readLine()
        try {
            println(client.addContact(UID(userID?.toLong() ?: 0)))
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    private fun dumb() {
        println("Sdohny Tvar'")
    }
}