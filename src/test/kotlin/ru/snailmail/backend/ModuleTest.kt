package ru.snailmail.backend

import com.google.gson.Gson
import io.ktor.application.Application
import io.ktor.auth.UserPasswordCredential
import io.ktor.http.*
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
        val gson = Gson()
        val call = handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(gson.toJson(cred))
        }
        assertEquals(HttpStatusCode.OK, call.response.status())
        assertNotNull(Master.findUserByLogin("Anton"))
        assertNull(Master.findUserByLogin("Ne Anton"))
    }
}