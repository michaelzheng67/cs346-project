package com.example

import com.example.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import net.codebot.models.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals

// /pins/insert
class PinInsertTest {

    @Before
    fun setUp() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Trips)
            SchemaUtils.create(Pins)
        }
    }

    @Test
    fun testPinInsert() = testApplication {
        application {
            module(true)
        }

        // create user
        val userRequest = UserInsert(true,"test@example.com", "password123", "Test User", "Bio", "Joined", "image_url", "auth_id", "avatar")
        val requestBody = Json.encodeToString(userRequest)

        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // create trip
        val tripRequest = TripInsert(1, "name", "description", "starttime", "endtime", 1, "image", "status")
        val tripBody = Json.encodeToString(tripRequest)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // test endpoint
        val pinRequest = PinInsert(1, 1, "city", "location", "description", "image")
        val pinBody = Json.encodeToString(pinRequest)

        client.post("/pin/insert") {
            contentType(ContentType.Application.Json)
            setBody(pinBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // verify results
        client.get("/user/1/pins").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("[{\"id\":1,\"userId\":1,\"tripID\":1,\"city\":\"city\"," +
                    "\"location\":\"location\",\"description\":\"description\"," +
                    "\"image\":\"image\"}]", bodyAsText())

        }


    }
}


// /trip/{id}/pins
class PinTripsTest {

    @Before
    fun setUp() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Trips)
            SchemaUtils.create(Pins)
        }
    }

    @Test
    fun testPinTrips() = testApplication {
        application {
            module(true)
        }

        // create user
        val userRequest = UserInsert(true,"test@example.com", "password123", "Test User", "Bio", "Joined", "image_url", "auth_id", "avatar")
        val requestBody = Json.encodeToString(userRequest)

        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // create trip
        val tripRequest = TripInsert(1, "name", "description", "starttime", "endtime", 1, "image", "status")
        val tripBody = Json.encodeToString(tripRequest)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // create pin
        val pinRequest = PinInsert(1, 1, "city", "location", "description", "image")
        val pinBody = Json.encodeToString(pinRequest)

        client.post("/pin/insert") {
            contentType(ContentType.Application.Json)
            setBody(pinBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // test endpoint
        client.get("/trip/1/pins").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("[{\"id\":1,\"userId\":1,\"tripID\":1,\"city\":\"city\"," +
                    "\"location\":\"location\",\"description\":\"description\"," +
                    "\"image\":\"image\"}]", bodyAsText())
        }
    }
}