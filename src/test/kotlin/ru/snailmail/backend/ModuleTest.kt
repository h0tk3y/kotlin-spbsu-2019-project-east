package ru.snailmail.backend

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import io.ktor.application.Application
import io.ktor.auth.Credential
import io.ktor.auth.UserPasswordCredential
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resolveResource
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class ModuleTest {
    @BeforeEach
    fun connect() {
        val connection = Database.connect(
            "jdbc:h2:./testdb",
            driver = "org.h2.Driver"
        )

        transaction(connection) {
            Data.init()
        }
    }

    @AfterEach
    fun clearDB() {
        Data.clear()
    }

    private fun handleRequestPost(engine: TestApplicationEngine, uri: String, cred: Credential): TestApplicationCall {
        return engine.handleRequest(HttpMethod.Post, uri) {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Gson().toJson(cred))
        }
    }

    private fun handleRequestPostWithToken(engine: TestApplicationEngine, uri: String, token: String, request: Request): TestApplicationCall {
        return engine.handleRequest(HttpMethod.Post, uri) {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Gson().toJson(request))
        }
    }

    private fun idByResponse(response: TestApplicationResponse): UID {
        return jacksonObjectMapper().readValue(response.content.toString())
    }

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
        val callRegister = handleRequestPost(this, "/register", cred)
        assertEquals(HttpStatusCode.OK, callRegister.response.status())

        val callUsers = handleRequest(HttpMethod.Get, "/users")
        assert(callUsers.response.content.toString().contains("Anton"))
        assert(!callUsers.response.content.toString().contains("Grisha"))

        assertEquals(
            HttpStatusCode.Conflict,
            handleRequestPost(this, "/register", cred).response.status()
        )
    }

    @Test
    fun testLogin() = withTestApplication(Application::module) {
        val cred = UserPasswordCredential("Anton", "password")
        val callRegister = handleRequestPost(this, "/register", cred)
        val credWrongLogin = UserPasswordCredential("Grisha", "password")
        val credWrongPassword = UserPasswordCredential("Anton", "qwerty")
        assertEquals(
            HttpStatusCode.BadRequest,
            handleRequestPost(this, "/login", credWrongLogin).response.status()
        )
        assertEquals(
            HttpStatusCode.BadRequest,
            handleRequestPost(this, "/login", credWrongPassword).response.status()
        )
        val callLogin = handleRequestPost(this, "/login", cred)
        assertEquals(
            idByResponse(callRegister.response).id,
            JwtConfig.verifier.verify(callLogin.response.content?.split('$')!![0]).subject.toLong()
        )
    }

    @Test
    fun testCreateLichka() = withTestApplication(Application::module) {
        val cred1 = UserPasswordCredential("Anton1", "password")
        val registerUser1 = handleRequestPost(this, "/register", cred1)
        val user1Id = idByResponse(registerUser1.response)

        val cred2 = UserPasswordCredential("Anton2", "password")
        val registerUser2 = handleRequestPost(this, "/register", cred2)
        val user2Id = idByResponse(registerUser2.response)

        val cred3 = UserPasswordCredential("Anton3", "password")
        val registerUser3 = handleRequestPost(this, "/register", cred3)
        val user3Id = idByResponse(registerUser3.response)

        val token = handleRequestPost(this, "/login", cred1).response.content?.split('$')!![0]
        val createLichkaWithUser2 = handleRequestPostWithToken(this, "/createLichka", token, CreateLichkaRequest(user2Id))
        val createLichkaWithUser3 = handleRequestPostWithToken(this, "/createLichka", token, CreateLichkaRequest(user3Id))

        assertEquals(HttpStatusCode.OK, createLichkaWithUser2.response.status())
        assertEquals(HttpStatusCode.OK, createLichkaWithUser3.response.status())
        assertNotEquals(idByResponse(createLichkaWithUser2.response), idByResponse(createLichkaWithUser3.response))

    }
}