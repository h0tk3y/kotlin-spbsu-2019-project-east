package ru.snailmail.frontend
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import ru.snailmail.backend.*

import io.ktor.auth.UserPasswordCredential
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.JacksonConverter
import io.ktor.jackson.jackson
import kotlinx.css.input
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL
import java.net.URLEncoder

private val objectMapper = jacksonObjectMapper().registerModule(KotlinModule(nullToEmptyCollection = true))

class Client {

    var token : String? = null

    lateinit var user: User
        private set

    fun greetings() : String?{
        return sendGetRequest("")
    }

    //remove later
    fun getUsers() : List<User>{
        val rawResponse = sendGetRequest("users")
        rawResponse ?: return emptyList()
        val response : List<User> = objectMapper.readValue(rawResponse)
        return response
    }

    fun getChats() : String? {
        val outputBytes = objectMapper.writeValueAsBytes("")
        val rawResponse = sendPostRequest("chats", outputBytes)
//        rawResponse ?: return emptyList()
//        val response : List<Chat> = objectMapper.readValue(rawResponse)
        return rawResponse
    }

    fun register(creds: UserPasswordCredential) : String?{
        val outputBytes = objectMapper.writeValueAsBytes(creds)
        return sendPostRequest("register", outputBytes) //returns Json {UserId}
    }

    fun logIn(creds: UserPasswordCredential) : String? {
        val outputBytes = objectMapper.writeValueAsBytes(creds)
        var res = "Logged in as ${creds.name}"

        try {
            token = sendPostRequest("login", outputBytes)
        } catch (e : IllegalArgumentException) {
            throw e
        }
        return res
    }

    fun sendMessage(chatId: UID, text: String): String? {
        val outputBytes = objectMapper.writeValueAsBytes(SendMessageRequest(chatId, text))
        return sendPostRequest("sendMessage", outputBytes)
    }

    //change interface (enter friend's name)
    fun createLichka(friendId: UID) {
        val outputBytes = objectMapper.writeValueAsBytes(CreateLichkaRequest(friendId))
        sendPostRequest("createLichka", outputBytes)
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

    fun addToContacts() {

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

            if (responseCode != HTTP_OK) throw IllegalArgumentException(readResponse(errorStream))

            response = readResponse(inputStream)

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