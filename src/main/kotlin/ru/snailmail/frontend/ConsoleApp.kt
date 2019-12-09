package ru.snailmail.frontend

import io.ktor.auth.UserPasswordCredential
import ru.snailmail.backend.UID

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
                "exit" -> {
                    flag = false
                }
                else -> dumb()
            }
        }
    }

    private fun login() {
        println("Enter your name: ")
        var name = readLine()
        println("Enter your password: ")
        var pass = readLine()
        println(client.logIn(UserPasswordCredential(name ?: "", pass ?: "")))
    }

    private fun register() {
        println("Enter your name: ")
        var name = readLine()
        println("Enter your password: ")
        var pass = readLine()
        println(client.register(UserPasswordCredential(name ?: "", pass ?: "")))
    }

    private fun help() {
        print(
            "Possible commands:\n" +
                    "\tlogin          Залогиниться\n" +
                    "\tregister       Зарегистрироваться\n" +
                    "\tcreateLichka   Создать диалог"
        )
    }

    private fun getUsers() {
        for (user in client.getUsers()) {
            println(user.name)
        }
    }

    private fun createLichka() {
        println("Enter your friend's id:")
        var name = readLine()
        client.createLichka(UID(name?.toLong() ?: 0))
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