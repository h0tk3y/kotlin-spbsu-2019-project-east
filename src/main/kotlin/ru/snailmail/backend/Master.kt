package ru.snailmail.backend

fun main() {
    val login = "boryan";
    Master.addUser(login);
}

final object Master {
    private val users = mutableListOf<User>();
    private val chats = mutableListOf<Chat>();
    private var lastId = 0;

    fun addUser(userLogin: String) {
        for (user in users) {
            if (user.name == userLogin) {
                throw IllegalArgumentException();
            }
        }
        users.add(User(userLogin))
    }



}