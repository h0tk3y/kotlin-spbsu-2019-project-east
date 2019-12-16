package ru.snailmail.frontend

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ru.snailmail.backend.*

import io.ktor.auth.UserPasswordCredential
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.net.URL
import java.net.URLEncoder

private val objectMapper = jacksonObjectMapper()

class Client {

    var token : String? = null

    lateinit var user: User
        private set

    fun logout() {
        clearDB()
        token = null
    }

    fun greetings() : String?{
        return sendGetRequest("")
    }

    // TODO: remove later
    fun getUsers() : List<User>{
        val rawResponse = sendGetRequest("users")
        rawResponse ?: return emptyList()
        val response : List<User> = objectMapper.readValue(rawResponse)
        return response
    }

    fun getChats() : List<UnionChat> {
        val outputBytes = objectMapper.writeValueAsBytes("")
        val rawResponse = sendPostRequest("chats", outputBytes)
        rawResponse ?: return emptyList()
        val response : List<UnionChat> = objectMapper.readValue(rawResponse)
        return response
    }

    fun getChatMessages(chatId: UID) : List<Message>? {
        val outputBytes = objectMapper.writeValueAsBytes(ChatRequest(chatId))
        var rawResponse :String? = null
        try {
            rawResponse = sendPostRequest("showMessages", outputBytes)
        } catch (e : java.lang.IllegalArgumentException) {
            println(e.message)
        }
        rawResponse ?: return null

        val response: List<Message> = objectMapper.readValue(rawResponse)
        return response
    }

    fun register(creds: UserPasswordCredential) : String?{
        val outputBytes = objectMapper.writeValueAsBytes(creds)
        return sendPostRequest("register", outputBytes) //returns Json {UserId}
    }

    fun connectDB() {
        val connection = Database.connect(
            "jdbc:h2:./clientDB",
            driver = "org.h2.Driver"
        )
        transaction(connection) {
            ClientData().init()
        }
        println("Connected!")
    }

    fun clearDB() {
        val connection = Database.connect(
            "jdbc:h2:./clientDB",
            driver = "org.h2.Driver"
        )
        transaction(connection) {
            ClientData().clear()
        }
        println("Disconnected!")
    }

    fun logIn(creds: UserPasswordCredential) : String? {
        val outputBytes = objectMapper.writeValueAsBytes(creds)
        connectDB()
        try {
            val responce = sendPostRequest("login", outputBytes)
            token = responce?.split('$')!![0]
            user = objectMapper.readValue(responce.split('$')[1])
        } catch (e : IllegalArgumentException) {
            throw e
        }
        return "Logged in as ${creds.name}\n"
    }

    fun sendMessage(chatId: UID, text: String): String? {
        val outputBytes = objectMapper.writeValueAsBytes(SendMessageRequest(chatId, text))
        return sendPostRequest("sendMessage", outputBytes)
    }

    fun createLichka(friendLogin: String) {
        val outputBytes = objectMapper.writeValueAsBytes(CreateLichkaRequest(friendLogin))
        sendPostRequest("createLichka", outputBytes)
    }

    fun createPublicChat(name: String) {
        val outputBytes = objectMapper.writeValueAsBytes(CreatePublicChatRequest(name))
        sendPostRequest("createPublicChat", outputBytes)
    }

    fun inviteUser(chatID: UID, userID: UID) {
        val outputBytes = objectMapper.writeValueAsBytes(InviteMemberRequest(chatID, userID))
        sendPostRequest("inviteMember", outputBytes)
    }

    fun getContacts() : List<Contact> {
        val outputBytes = objectMapper.writeValueAsBytes("")
        val rawResponse = sendPostRequest("contacts", outputBytes)
        rawResponse ?: return emptyList()
        return objectMapper.readValue(rawResponse)
    }

    fun addContact(userID : UID) {
        val outputBytes = objectMapper.writeValueAsBytes(AddOrDeleteContactRequest(userID))
        sendPostRequest("addContact", outputBytes)
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

            if (responseCode == HTTP_UNAUTHORIZED) {
                throw IllegalArgumentException("Unauthorized")
            }

            if (responseCode != HTTP_OK) {
                val res: String = if (errorStream == null) {
                    "Error"
                } else {
                    readResponse(errorStream)
                }
                throw IllegalArgumentException(res)
            }

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