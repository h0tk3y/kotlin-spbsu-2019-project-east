package ru.snailmail.backend

import com.google.gson.Gson
import io.ktor.application.Application
import io.ktor.auth.UserPasswordCredential
import io.ktor.http.*
import io.ktor.request.receive
import io.ktor.request.receiveText
import io.ktor.response.respondText
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

import io.ktor.server.testing.*


class ModuleTest {
    @Test
    fun testWestLohi() = withTestApplication(Application::module) {
        with(handleRequest(HttpMethod.Get, "/")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "  <head>\n" +
                        "    <title>West - Lohi!</title>\n" +
                        "  </head>\n" +
                        "  <body>\n" +
                        "    <h1>West - lohi!</h1>\n" +
                        "  </body>\n" +
                        "</html>\n", response.content
            )
        }
    }
    @Test
    fun testRegister() = withTestApplication(Application::module) {
        val cred = UserPasswordCredential("Anton", "password")
        val call = handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Gson().toJson(cred))
        }
        assertEquals(HttpStatusCode.OK, call.response.status())
        assertNotNull(Master.findUserByLogin("Anton"))
        assertNull(Master.findUserByLogin("Ne Anton"))
    }

    private fun userIdByResponse(response: TestApplicationResponse) : UID {
        return UID(response.content.toString().drop(16).take(response.content.toString().drop(16).length - 2).toLong())
    }
    private fun lichkaIdByResponse(response: TestApplicationResponse) : UID {
        return UID(response.content.toString().drop(17).take(response.content.toString().drop(17).length - 2).toLong())
    }

    @Test
    fun testCreateLichka() = withTestApplication(Application::module) {
        val cred1 = UserPasswordCredential("Anton1", "password")
        val registerUser1 = handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Gson().toJson(cred1))
        }
        val user1Id = userIdByResponse(registerUser1.response)

        val cred2 = UserPasswordCredential("Anton2", "password")
        val registerUser2 = handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Gson().toJson(cred2))
        }

        val cred3 = UserPasswordCredential("Anton3", "password")
        val registerUser3 = handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Gson().toJson(cred3))
        }
        val user3Id = userIdByResponse(registerUser3.response)

        val user2Id = userIdByResponse(registerUser2.response)
        val token = handleRequest(HttpMethod.Post, "/login") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Gson().toJson(cred1))
        }
        val tokenOfUser1 = token.response.content.toString().drop(12)
        val params = CreateLichkaRequest(tokenOfUser1, user2Id)
        val createLichka = handleRequest(HttpMethod.Post, "/createLichka") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Gson().toJson(params))
        }
        val lichka1Id = lichkaIdByResponse(createLichka.response)
        assertEquals(HttpStatusCode.OK, createLichka.response.status())
        assertNotNull(Master.findChatById(lichka1Id))
        val lichka1 = Master.findChatById(lichka1Id)
        assert(Master.findUserById(user1Id)?.chats?.contains(lichka1) ?: false)
        assert(Master.findUserById(user2Id)?.chats?.contains(lichka1) ?: false)
        assert(!(Master.findUserById(user3Id)?.chats?.contains(lichka1) ?: true))
    }

}