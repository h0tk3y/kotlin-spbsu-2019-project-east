package ru.snailmail.frontend

import io.ktor.auth.UserPasswordCredential

fun main() {
    val ca = ConsoleApp()
    ca.runner()
}

class ConsoleApp {
    private val client = Client()
    fun runner() {

        var flag = true
        //    println("Hello, I'm SnailMail! Let's start!")
        //    println("(If you don't know what to do: write \"-h\")")
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
                "createLichka" -> {
                    createLichka()
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
                    "\tregister       Зарегистрироваться\n"
        )
    }

    private fun getUsers() {
        println(client.getUsers())
    }

    private fun createLichka() {
        println("Enter your friend's name:")

        createLichka()
    }

    private fun dumb() {
        println("Sdohny Tvar'")
    }
}