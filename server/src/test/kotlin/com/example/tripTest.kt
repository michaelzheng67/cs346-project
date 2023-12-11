package com.example

import com.example.plugins.Pins
import com.example.plugins.Trips
import com.example.plugins.Users
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.codebot.models.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// /trips
class TripTest {

    @Before
    fun setUp() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Trips)
        }
    }

    @Test
    fun testTrip() = testApplication {
        application {
            module(true)
        }

        // insert user
        val userRequest = UserInsert(
            true,
            "test@example.com",
            "password123",
            "Test User",
            "Bio",
            "Joined",
            "image_url",
            "auth_id",
            "avatar"
        )
        val requestBody = Json.encodeToString(userRequest)

        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // insert trips
        val trip1 = TripInsert(1, "name", "description", "starttime", "endtime", 1, "image", "status")
        val tripBody1 = Json.encodeToString(trip1)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripBody1)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        val trip2 = TripInsert(1, "name2", "description2", "starttime2", "endtime2", 1, "image", "status")
        val tripBody2 = Json.encodeToString(trip2)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripBody2)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // test endpoint
        client.get("/trips").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                "[{\"id\":1,\"userId\":1,\"name\":\"name\",\"description\":\"description\"," +
                        "\"startTime\":\"starttime\",\"endTime\":\"endtime\",\"score\":1,\"image\":\"image\"," +
                        "\"status\":\"status\"},{\"id\":2,\"userId\":1,\"name\":\"name2\",\"description\":\"description2\"," +
                        "\"startTime\":\"starttime2\",\"endTime\":\"endtime2\",\"score\":1,\"image\":\"image\"," +
                        "\"status\":\"status\"}]", bodyAsText()
            )
        }
    }
}


// /trips/insert
class TripInsertTest {

    @Before
    fun setUp() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Trips)
        }
    }

    @Test
    fun testTripInsert() = testApplication {
        application {
            module(true)
        }

        // insert user
        val userRequest = UserInsert(
            true,
            "test@example.com",
            "password123",
            "Test User",
            "Bio",
            "Joined",
            "image_url",
            "auth_id",
            "avatar"
        )
        val requestBody = Json.encodeToString(userRequest)

        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // test endpoint
        val trip1 = TripInsert(1, "name", "description", "starttime", "endtime", 1, "image", "status")
        val tripBody1 = Json.encodeToString(trip1)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripBody1)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // verify result
        client.get("/trips").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                "[{\"id\":1,\"userId\":1,\"name\":\"name\",\"description\":\"description\"," +
                        "\"startTime\":\"starttime\",\"endTime\":\"endtime\",\"score\":1,\"image\":\"image\"," +
                        "\"status\":\"status\"}]", bodyAsText()
            )
        }
    }
}


// /trips/{id}
class TripDeleteTest {

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
    fun testTripDelete() = testApplication {
        application {
            module(true)
        }

        // insert user
        val userRequest = UserInsert(
            true,
            "test@example.com",
            "password123",
            "Test User",
            "Bio",
            "Joined",
            "image_url",
            "auth_id",
            "avatar"
        )
        val requestBody = Json.encodeToString(userRequest)

        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // insert trip first
        val tripRequest = TripInsert(1, "name", "description", "starttime", "endtime", 1, "image", "status")
        val tripBody = Json.encodeToString(tripRequest)
        val pinRequest = PinInsert(1, 1, "city", "location", "description", "image")
        val pinBody = Json.encodeToString(pinRequest)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        client.post("/pin/insert") {
            contentType(ContentType.Application.Json)
            setBody(pinBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // delete endpoint
        client.delete("/trips/1").apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // get all pins
        client.get("/trips").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("[]", bodyAsText())
        }
    }
}


// /trip/top/{n}
class TripTopTest {

    @Before
    fun setUp() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Trips)
        }
    }

    @Test
    fun testTripTop() = testApplication {
        application {
            module(true)
        }

        // insert user
        val userRequest = UserInsert(
            true,
            "test@example.com",
            "password123",
            "Test User",
            "Bio",
            "Joined",
            "image_url",
            "auth_id",
            "avatar"
        )
        val requestBody = Json.encodeToString(userRequest)

        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // create three trips
        val trip1 = TripInsert(1, "name", "description", "starttime", "endtime", 100, "image", "status")
        val tripBody1 = Json.encodeToString(trip1)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripBody1)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        val trip2 = TripInsert(1, "name2", "description2", "starttime2", "endtime2", 99, "image", "status")
        val tripBody2 = Json.encodeToString(trip2)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripBody2)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        val trip3 = TripInsert(1, "name3", "description3", "starttime3", "endtime3", 98, "image", "status")
        val tripBody3 = Json.encodeToString(trip3)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripBody3)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // test endpoint, should return trip1 and trip2 as they have highest 2 scores
        client.get("/trip/top/2").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                "[{\"id\":1,\"userId\":1,\"name\":\"name\",\"description\":\"description\"," +
                        "\"startTime\":\"starttime\",\"endTime\":\"endtime\",\"score\":100,\"image\":\"image\"," +
                        "\"status\":\"status\"},{\"id\":2,\"userId\":1,\"name\":\"name2\",\"description\":\"description2\"," +
                        "\"startTime\":\"starttime2\",\"endTime\":\"endtime2\",\"score\":99,\"image\":\"image\"," +
                        "\"status\":\"status\"}]", bodyAsText()
            )
        }
    }
}


// /trip/update/{id}
class TripUpdateTest {

    @Before
    fun setUp() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Trips)
        }
    }

    @Test
    fun testTripUpdate() = testApplication {
        application {
            module(true)
        }

        // insert user
        val userRequest = UserInsert(
            true,
            "test@example.com",
            "password123",
            "Test User",
            "Bio",
            "Joined",
            "image_url",
            "auth_id",
            "avatar"
        )
        val requestBody = Json.encodeToString(userRequest)

        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // insert trip first
        val tripRequest = TripInsert(1, "name", "description", "starttime", "endtime", 1, "image", "status")
        val tripBody = Json.encodeToString(tripRequest)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // test endpoint
        val tripUpdate =
            TripUpdate(1, "new name", "new description", "new starttime", "new endtime", 2, "new image", "new status")
        val updateBody = Json.encodeToString(tripUpdate)

        client.patch("/trip/update/1") {
            contentType(ContentType.Application.Json)
            setBody(updateBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                "{\"id\":1,\"userId\":1,\"name\":\"new name\"," +
                        "\"description\":\"new description\",\"startTime\":\"new starttime\"," +
                        "\"endTime\":\"new endtime\",\"score\":2,\"image\":\"new image\"," +
                        "\"status\":\"new status\"}", bodyAsText()
            )
        }
    }
}


// /trips/{id}/pins-delete
class TripDeletePinsTest {

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
    fun testTripDeletePins() = testApplication {
        application {
            module(true)
        }

        // insert user
        val userRequest = UserInsert(
            true,
            "test@example.com",
            "password123",
            "Test User",
            "Bio",
            "Joined",
            "image_url",
            "auth_id",
            "avatar"
        )
        val requestBody = Json.encodeToString(userRequest)

        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // insert trip
        val trip1 = TripInsert(1, "name", "description", "starttime", "endtime", 100, "image", "status")
        val tripBody1 = Json.encodeToString(trip1)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripBody1)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // insert multiple pins
        val pin1 = PinInsert(1, 1, "city1", "location1", "description1", "image1")
        val pinBody1 = Json.encodeToString(pin1)

        client.post("/pin/insert") {
            contentType(ContentType.Application.Json)
            setBody(pinBody1)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        val pin2 = PinInsert(1, 1, "city2", "location2", "description2", "image2")
        val pinBody2 = Json.encodeToString(pin2)

        client.post("/pin/insert") {
            contentType(ContentType.Application.Json)
            setBody(pinBody2)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        val pin3 = PinInsert(1, 1, "city3", "location3", "description3", "image3")
        val pinBody3 = Json.encodeToString(pin3)

        client.post("/pin/insert") {
            contentType(ContentType.Application.Json)
            setBody(pinBody3)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // test endpoint
        client.delete("/trip/1/pins-delete").apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // verify results
        client.get("/trip/1/pins").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("[]", bodyAsText())
        }

    }
}


class TripDataCheck {
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
    fun testTrip() = testApplication {
        application {
            module(true)
        }

        // insert user
        val userRequest = UserInsert(
            true,
            "test@example.com",
            "password123",
            "Test User",
            "Bio",
            "Joined",
            "image_url",
            "auth_id",
            "avatar"
        )
        val requestBody = Json.encodeToString(userRequest)

        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // insert trip
        val trip1 = TripInsert(1, "name", "description", "starttime", "endtime", 100, "image", "status")
        val tripBody1 = Json.encodeToString(trip1)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripBody1)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // insert trip
        val trip2 = TripInsert(1, "name", "description", "starttime", "endtime", 100, "image", "status")
        val tripBody2 = Json.encodeToString(trip2)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripBody2)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // not all trips
        val userTrips = emptyList<Int>()
        val requestBody1 = Json.encodeToString(userTrips)

        client.post("/trips/data-check") {
            contentType(ContentType.Application.Json)
            setBody(requestBody1)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(false, Json.decodeFromString<Boolean>(bodyAsText()))

        }


        // test endpoint
        var allTrips: List<Trip>
        client.get("/trips").apply {
            allTrips = Json.decodeFromString<List<Trip>>(bodyAsText())
        }

        client.post("/trips/data-check") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(allTrips.map { it.id }))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue { Json.decodeFromString(bodyAsText()) }
        }

    }
}