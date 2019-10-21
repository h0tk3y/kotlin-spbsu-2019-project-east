package ru.snailmail.frontend

fun main() {

    val client = Client()
    var flag = true
    println("Hello, I'm SnailMail! Let's start!")
    println("(If you don't know what to do: write \"-h\")")

    while (flag) {
        var answer = readLine()
        when (answer) {
            "-h" -> help()
            "login" -> {login()
                        flag = false}
            "register" -> {register()
                           flag = false}
            else -> dumb()
        }
    }
}

fun login() {
    println("It's great")
}

fun register() {
    println("It's bad, let's register")
}

fun help() {
    print("Possible commands:\n" +
            "\tlogin          Залогиниться\n" +
            "\tregister       Зарегистрироваться\n")
}

fun dumb() {
    println("Sdohny Tvar'")
}
