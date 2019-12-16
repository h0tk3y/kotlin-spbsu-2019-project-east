package ru.snailmail.frontend

import io.ktor.auth.UserPasswordCredential
import ru.snailmail.backend.Chat
import ru.snailmail.backend.Contact
import ru.snailmail.backend.Message
import ru.snailmail.backend.UID
import java.lang.Exception

const val ANSI_RESET = "\u001B[0m"
const val ANSI_BLACK = "\u001B[30m"
const val ANSI_RED = "\u001B[31m"
const val ANSI_GREEN = "\u001B[32m"
const val ANSI_YELLOW = "\u001B[33m"
const val ANSI_BLUE = "\u001B[34m"
const val ANSI_PURPLE = "\u001B[35m"
const val ANSI_CYAN = "\u001B[36m"
const val ANSI_WHITE = "\u001B[37m"

fun main() {
    ConsoleApp().runner()
}

class ConsoleApp {
    private val client = Client()
    fun runner() {

        var flag = true
        try {
            println(client.greetings())
        } catch (e : Exception) {
            println("Bad connection")
        }

        while (flag) {
            val answer = readLine()
            if (loggedIn()) {
                when (answer) {
                    "-h" -> help()
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
                    "get chat messages" -> {
                        getChatMessages()
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
                    "logout" -> {
                        logout()
                    }
                    else -> dumb()
                }
            } else {
                when (answer) {
                    "-h" -> help()
                    "login" -> {
                        login()
                    }
                    "register" -> {
                        register()
                    }
                    else -> println("Sign in or register, type -h for help")
                }
            }
        }
    }

    private fun loggedIn() : Boolean{
        return client.token != null
    }

    private fun help() {
        if (loggedIn()) {
            print(
                "Possible commands:\n" +
                        "\tcreate lichka        Создать диалог\n" +
                        "\tsend message         Послать сообщение\n" +
                        "\tget chats            Список чатов\n" +
                        "\tget chat messages    Сообщения чата\n" +
                        "\tcreate chat          Создать беседу\n" +
                        "\tinvite member        Пригласить в беседу\n" +
                        "\tget contacts         Посмотреть контакты\n" +
                        "\tadd contact          Добавить контакт\n" +
                        "\tlogout\n" +
                        "\texit\n"
            )
        } else {
            print(
                "Possible commands:\n" +
                        "\tlogin                Залогиниться\n" +
                        "\tregister             Зарегистрироваться\n" +
                        "\texit\n"
            )
        }
    }

    private fun messagePrettyPrint(msg : Message) {
        val from = client.getUsers().find { it.userID == msg.from }
        if (from!!.userID == client.user.userID) {
            println("\t\t" + msg.time)
            println("\t\t" + ANSI_BLUE + "You" + ANSI_RESET)
            println("\t\t" + ANSI_GREEN + msg.text + ANSI_RESET)
        } else {
            println( msg.time)
            println(ANSI_BLUE + from.name + ANSI_RESET)
            println(ANSI_GREEN + msg.text + ANSI_RESET)
        }
        println()
    }

    private fun chatPrettyPrint(chat : Chat) {
        println(chat.javaClass)
    }

    private fun chatsPrettyPrint() {

    }

    private fun getChatMessages() {
        println("Enter chat Id")
        val n : Long?  = readLine()?.toLong()
        if (n == null) {
            println("Incorrect input")
            return
        }
        val msgs = client.getChatMessages(UID(n))
        if (msgs == null ) {
            println("Something went wrong...")
            return
        }
        if (msgs.isEmpty()) {
            println("No messages yet")
        }
        msgs.forEach { messagePrettyPrint(it) }
    }

    private fun logout() {
        client.logout()
        println("You are logged out")
    }

    private fun login() {
        if (loggedIn()) {
            println("You are already logged in!")
            return
        }
        println("Enter your name: ")
        val name = readLine()
        println("Enter your password: ")
        val pass = readLine()
        try {
            print(client.logIn(UserPasswordCredential(name ?: "", pass ?: "")))
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    private fun register() {
        println("Enter your name: ")
        val name = readLine()
        println("Enter your password: ")
        val pass = (System.console()?.readPassword() ?: readLine())?.toString()
        try {
            client.register(UserPasswordCredential(name ?: "", pass ?: ""))
            println("User $name signed up!")
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    private fun getUsers() {
        val users = client.getUsers()
        if (users.isEmpty()) println("No users")
        for (user in users) {
            println(user.name + ", " + "ID=${user.userID.id}")
        }
    }

    private fun createLichka() {
        println("Enter your friend's name:")
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
            client.sendMessage(UID(chatId?.toLong() ?: 0), text ?: "")
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
            client.inviteUser(UID(chatID?.toLong() ?: 0), UID(userID?.toLong() ?: 0))
            println("Member invited")
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    private fun getChats() {
        try {
            val chats = client.getChats()
            if (chats.isEmpty()) {
                println(ANSI_CYAN + "You haven't got anybody to chat with" + ANSI_RESET)
                return
            }
            println(ANSI_BLUE + "Your chats:" + ANSI_RESET)
            for (chat in chats) {
                println(chat.name + " ID = " + chat.chatId.id)
            }
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    private fun getContacts() {
        val contacts: List<Contact>
        try {
            contacts = client.getContacts()
            if (contacts.isEmpty()) {
                println(ANSI_CYAN + "You haven't got friends" + ANSI_RESET)
                return
            }
            println(ANSI_BLUE + "Your contacts:" + ANSI_RESET)
            for (contact in contacts) {
                println(contact.preferredName + " ID = " + contact.userId.id)
            }
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    private fun addContact() {
        println("Enter contact's id:")
        val userID = readLine()
        try {
            client.addContact(UID(userID?.toLong() ?: 0))
            println("Contact added")
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    private fun dumb() {
        println("Sdohny Tvar'")
    }
}