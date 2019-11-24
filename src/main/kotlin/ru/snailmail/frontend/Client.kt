package ru.snailmail.frontend
import com.google.gson.Gson
import ru.snailmail.backend.*

import io.ktor.auth.UserPasswordCredential
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.css.input
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL
import java.net.URLEncoder

class Client {

    var token : String? = null

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
        val outputBytes = Gson().toJson(creds).toByteArray(charset("UTF-8"))
        return sendPostRequest("register", outputBytes) //returns Json {UserId}
    }

    fun logIn(creds: UserPasswordCredential) : String? {
        val outputBytes = Gson().toJson(creds).toByteArray(charset("UTF-8"))
        var res = "OK"

        try {
            token = sendPostRequest("login", outputBytes)
        } catch (e : Exception) {
            res = "Error"
        }
        return res
    }

    fun sendMessage(c: Chat, text: String): UID {
        if (!::user.isInitialized) {
            throw IllegalAccessException("Not registered")
        }
        if (!Data.userInChat(user.userID, c.chatID)) {
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
        var response:String
        with(mURL.openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            response = readResponse(inputStream)
        }
        return response
    }

    private fun sendPostRequest(addr:String, outputBytes:ByteArray) : String?{

        val url = URL("http://127.0.0.1:8080/$addr")

        lateinit
        var response:String

        with(url.openConnection() as HttpURLConnection) {
            doOutput = true
            requestMethod = "POST"

            setRequestProperty(HttpHeaders.ContentType, ContentType.Application.Json.toString())

            if (token != null) {
                setRequestProperty(HttpHeaders.Authorization, "Bearer ${token}")
            }

            outputStream.write(outputBytes)

            if (responseCode != HTTP_OK) throw java.lang.IllegalArgumentException("Something went wrong")

            response = readResponse(inputStream)
//            println(responseMessage)
        }

        return response

    }

    private fun readResponse(inputStream : InputStream) : String {
        BufferedReader(InputStreamReader(inputStream)).use {
            val res = StringBuffer()

            var inputLine = it.readLine()
            while (inputLine != null) {
                res.append(inputLine)
                inputLine = it.readLine()
            }
            it.close()
            return res.toString()
        }
    }
}