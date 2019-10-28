package ru.snailmail.frontend
import ru.snailmail.backend.*

import io.ktor.auth.UserPasswordCredential
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
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

    fun register(creds: UserPasswordCredential) {
//        var params = "register?name=" + creds.name + "&password=" + creds.password
        return sendPostRequest(creds.name, creds.password)
    }

    fun logIn(creds: UserPasswordCredential) {
        user = Master.logIn(creds)
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

//        var reqParam = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(userName, "UTF-8")
//        reqParam += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8")
        var reqParam = URLEncoder.encode(param, "UTF-8")

        val mURL = URL("http://127.0.0.1:8080/$reqParam")

        lateinit
        var response:String;

        with(mURL.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "GET"

//            println("URL : $url")
//            println("Response Code : $responseCode")

            BufferedReader(InputStreamReader(inputStream)).use {
                val res = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    res.append(inputLine)
                    inputLine = it.readLine()
                }
                it.close()
//                println("Response : $res")
                response = res.toString()
            }
        }
        return response
    }

    fun sendPostRequest(userName:String, password:String) {

        var reqParam = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(userName, "UTF-8")
        reqParam += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8")
//        var reqParam = URLEncoder.encode(param, "UTF-8")
        val mURL = URL("http://127.0.0.1/register?")

        with(mURL.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "POST"

            val wr = OutputStreamWriter(getOutputStream());
            wr.write(reqParam);
            wr.flush();

            println("URL : $url")
            println("Response Code : $responseCode")

            BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                it.close()
                println("Response : $response")
            }
        }
    }
}