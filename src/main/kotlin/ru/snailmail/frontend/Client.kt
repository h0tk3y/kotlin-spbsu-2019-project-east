package ru.snailmail.frontend
import com.google.gson.Gson
import ru.snailmail.backend.*

import io.ktor.auth.UserPasswordCredential
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL
import java.net.URLEncoder

class Client {
    // TODO: request Master using network
    lateinit var user: User
        private set

    fun greetings() : String?{
        return sendGetRequest("")
    }

    //remove later
    fun getUsers() : String?{
        return sendGetRequest("users")
    }

    fun register(creds: UserPasswordCredential) : String?{
        return sendPostRequest("register", creds.name, creds.password)
    }

    fun logIn(creds: UserPasswordCredential) : String? {
        return sendPostRequest("login", creds.name, creds.password)
    }

    fun sendMessage(c: Chat, text: String): UID {
        if (!::user.isInitialized) {
            throw IllegalAccessException("Not registered")
        }
        if (!user.chats.contains(c)) {
            throw IllegalArgumentException("Chat doesn't exist")
        }
        return Master.sendMessage(user, c, text)
    }

    fun createLichka(user: User) {
        Master.createLichka(this.user, user)
    }

    fun createPublicChat(name: String) {
        if (!::user.isInitialized) {
            throw IllegalAccessException("Not registered")
        }
        Master.createPublicChat(user, name)
    }

    fun inviteUser(c: PublicChat, user: User) {
        Master.inviteUser(this.user, c, user)
    }

    private fun sendGetRequest(param: String) : String? {

        var reqParam = URLEncoder.encode(param, "UTF-8")
        val mURL = URL("http://127.0.0.1:8080/$reqParam")

        lateinit
        var response:String;

        with(mURL.openConnection() as HttpURLConnection) {

            requestMethod = "GET"

            BufferedReader(InputStreamReader(inputStream)).use {
                val res = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    res.append(inputLine)
                    inputLine = it.readLine()
                }
                it.close()
                response = res.toString()
            }
        }
        return response
    }

    private fun sendPostRequest(addr:String, userName:String, password:String) : String?{
        val cred = UserPasswordCredential(userName, password)
        val url = URL("http://127.0.0.1:8080/$addr")
        val con = url.openConnection() as HttpURLConnection

        con.doOutput = true
        con.requestMethod = "POST"
        con.setRequestProperty(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        val outputBytes = Gson().toJson(cred).toByteArray(charset("UTF-8"))
        con.outputStream.write(outputBytes)


        if (con.responseCode != HTTP_OK) throw java.lang.IllegalArgumentException("Something went wrong")

        val responseMsg = con.responseMessage
        val response = con.responseCode
        println(responseMsg)
        return responseMsg

    }
}