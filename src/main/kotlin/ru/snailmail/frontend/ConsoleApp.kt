package ru.snailmail.frontend

import io.ktor.auth.UserPasswordCredential
import ru.snailmail.backend.AlreadyExistsException
import ru.snailmail.backend.AlreadyInTheChatException
import ru.snailmail.backend.UID
import java.lang.IllegalArgumentException

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
            var answer = readLine()
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
                "send message" -> {
                    sendMessage()
                }
                "get chats" -> {
                    getChats()
                }
                "get chat messages" -> {
                    println("Enter chat Id")
                    getChatMessages()
                }
                "exit" -> {
                    flag = false
                }
                "logout" -> {
                    logout()
                }
                else -> dumb()
            }
        }
    }

    private fun getChatMessages() {
        val n :Int?  = readLine()?.toInt()
        if (n == null) {
            println("Incorrect input")
            return
        }
        client.getChatMessages(UID(n.toLong()))
    }

    private fun logout() {
        client.logout()
        println("You are logged out")
    }

    private fun login() {
        if (client.token != null) {
            println("You are already logged in!")
            return
        }
        println("Enter your name: ")
        var name = readLine()
        println("Enter your password: ")
        var pass = readLine()
        try {
            println(client.logIn(UserPasswordCredential(name ?: "", pass ?: "")))
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    private fun register() {
        println("Enter your name: ")
        var name = readLine()
        println("Enter your password: ")
        var pass = readLine()
        try {
            println(client.register(UserPasswordCredential(name ?: "", pass ?: "")))
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
                    "\tsend message     Послать сообщение\n" +
                    "\tget chats    Список чатов\n" +
                    "\tget chat messages    Сообщения чата\n" +
                    "\tlogout\n" +
                    "\texit\n"
        )
    }

    private fun getUsers() {
        for (user in client.getUsers()) {
            println(user.name + " " + "ID = ${user.userID.id}")
        }
    }

    private fun createLichka() {
        println("Enter your friend's id:")
        var name = readLine()
        try {
            client.createLichka(UID(name?.toLong() ?: 0))
            println("Lichka with $name created")
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    private fun sendMessage() {
        println("Enter chat id:")
        var chatId = readLine()
        println("Enter message:")
        var text = readLine()
        print(client.sendMessage(UID(chatId?.toLong() ?: 0), text ?: ""))
    }

    private fun getChats() {
        println(client.getChats())
    }

    private fun dumb() {
        println("Sdohny Tvar'")
    }
}