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
                "exit" -> {
                    flag = false
                }
                else -> dumb()
            }
        }
    }

    fun login() {
        println("It's great")
    }

    fun register() {
        println("Enter your name: ")
        var name = readLine()
        println("Enter your password: ")
        var pass = readLine()
        client.register(UserPasswordCredential(name ?: "", pass ?: ""))
//        println(UserPasswordCredential(name ?: "", pass ?: "").toString())
    }

    fun help() {
        print(
            "Possible commands:\n" +
                    "\tlogin          Залогиниться\n" +
                    "\tregister       Зарегистрироваться\n"
        )
    }

    fun getUsers() {
        println(client.getUsers())
    }

    fun dumb() {
        println("Sdohny Tvar'")
    }
}