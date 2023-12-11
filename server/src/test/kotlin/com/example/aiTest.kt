package com.example

import com.example.plugins.*
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.codebot.models.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

// Uncomment this only for local testing, as it is personal OpenAI key and don't want to run up
// bill from ci/ci pipeline constantly running these tests

//class AiTest {
//
//    @Before
//    fun setUp() {
//        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
//
//        transaction {
//            SchemaUtils.create(Users)
//            SchemaUtils.create(Trips)
//            SchemaUtils.create(Liked)
//        }
//    }
//
//    @Test
//    fun testAi() = testApplication {
//        application {
//            module(true)
//        }
//
//        // insert user
//        val userRequest = UserInsert(true,"test@example.com", "password123", "Test User", "Bio", "Joined", "image_url", "auth_id")
//        val requestBody = Json.encodeToString(userRequest)
//
//        client.post("/user/insert") {
//            contentType(ContentType.Application.Json)
//            setBody(requestBody)
//        }.apply {
//            assertEquals(HttpStatusCode.OK, status)
//            assertEquals("User inserted successfully!", bodyAsText())
//        }
//
//        // insert trips
//        val trip1 = TripInsert(1, "Italy", "description", "starttime", "endtime", 1, "image", "status")
//        val tripBody1 = Json.encodeToString(trip1)
//
//        client.post("/trip/insert") {
//            contentType(ContentType.Application.Json)
//            setBody(tripBody1)
//        }.apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//
//        val trip2 = TripInsert(1, "Manila", "description2", "starttime2", "endtime2", 1, "image", "status")
//        val tripBody2 = Json.encodeToString(trip2)
//
//        client.post("/trip/insert") {
//            contentType(ContentType.Application.Json)
//            setBody(tripBody2)
//        }.apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//
//        // test endpoint
//        client.get("/ai/1").apply {
//            assertEquals(HttpStatusCode.OK, status)
//            assertEquals("", bodyAsText())
//        }
//    }
//}




//class AiProfilerTest {
//
//    @Before
//    fun setUp() {
//        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
//
//        transaction {
//            SchemaUtils.create(Users)
//            SchemaUtils.create(Trips)
//            SchemaUtils.create(Liked)
//        }
//    }
//
//    @Test
//    fun testAiProfiler() = testApplication {
//        application {
//            module(true)
//        }
//
//        // insert user
//        val userRequest = UserInsert(true,"test@example.com", "password123", "Test User", "Bio", "Joined", "image_url", "auth_id", "avatar")
//        val requestBody = Json.encodeToString(userRequest)
//
//        client.post("/user/insert") {
//            contentType(ContentType.Application.Json)
//            setBody(requestBody)
//        }.apply {
//            assertEquals(HttpStatusCode.OK, status)
//            assertEquals("User inserted successfully!", bodyAsText())
//        }
//
//        // insert trips
//        val trip1 = TripInsert(1, "Italy", "description", "starttime", "endtime", 1, "image", "status")
//        val tripBody1 = Json.encodeToString(trip1)
//
//        client.post("/trip/insert") {
//            contentType(ContentType.Application.Json)
//            setBody(tripBody1)
//        }.apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//
//        val trip2 = TripInsert(1, "Manila", "description2", "starttime2", "endtime2", 1, "image", "status")
//        val tripBody2 = Json.encodeToString(trip2)
//
//        client.post("/trip/insert") {
//            contentType(ContentType.Application.Json)
//            setBody(tripBody2)
//        }.apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//
//        val trip3 = TripInsert(1, "New York City", "description2", "starttime2", "endtime2", 1, "image", "status")
//        val tripBody3 = Json.encodeToString(trip3)
//
//        client.post("/trip/insert") {
//            contentType(ContentType.Application.Json)
//            setBody(tripBody3)
//        }.apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//
//        // test endpoint
//        client.get("/ai/profiler/1").apply {
//            assertEquals(HttpStatusCode.OK, status)
//            assertEquals("", bodyAsText())
//        }
//    }
//}