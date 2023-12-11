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
import org.junit.After
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Contains all user object related tests

// / endpoint
class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting(true)
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Received", bodyAsText())
        }
    }
}

// /user/{id} endpoint
class GetUserTest {

    @Before
    fun setUp() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users)
        }
    }

    @Test
    fun testUserGet() = testApplication {
        application {
            module(true)
        }

        val userRequest =
            UserInsert(true, "test@example.com", "password123", "Test User", "Bio", "Joined", "image_url", "auth_id", "avatar")
        val requestBody = Json.encodeToString(userRequest)

        // Send a POST request to the /user/insert endpoint
        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // test endpoint
        client.get("/user/1").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                "{\"id\":1,\"email\":\"test@example.com\",\"password\":\"password123\"," +
                        "\"name\":\"Test User\",\"bio\":\"Bio\",\"joined\":\"Joined\",\"image\":\"image_url\"," +
                        "\"auth_id\":\"auth_id\",\"avatar\":\"avatar\"}", bodyAsText()
            )
        }
    }
}

// /user/exchange_ids/{auth_id} endpoint
class GetAuthIdTest {

    @Before
    fun setUp() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users)
        }
    }

    @Test
    fun testGetAuthId() = testApplication {
        application {
            module(true)
        }

        val userRequest =
            UserInsert(true, "test@example.com", "password123", "Test User", "Bio", "Joined", "image_url", "auth_id", "avatar")
        val requestBody = Json.encodeToString(userRequest)

        // Send a POST request to the /user/insert endpoint
        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // test endpoint
        client.get("/user/exchange_id/auth_id").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("1", bodyAsText())
        }
    }
}

class userExistsTest {

    @Before
    fun setUp() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users)
        }
    }

    @Test
    fun testUserExists() = testApplication {
        application {
            module(true)
        }

        val userRequest =
            UserInsert(
                true,
                "test2@example.com",
                "password1234",
                "Test User 2",
                "Bio abcd",
                "Joined 123",
                "image_url",
                "test_auth_id",
                "avatar"
            )
        val requestBody = Json.encodeToString(userRequest)

        // Send a POST request to the /user/insert endpoint
        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // test endpoint
        client.get("/user/exists/test_auth_id").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("true", bodyAsText())
        }

        client.get("/user/exists/6946514651asdguio1hgbuibgn").apply { // shouldn't exist
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("false", bodyAsText())
        }
    }
}


// /user/insert endpoint
class InsertUserTest {

    @Before
    fun setUp() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users)
        }
    }

    @Test
    fun testUserInsert() = testApplication {
        application {
            module(true)
        }


        // Define the user data to insert
        val userRequest =
            UserInsert(true, "test@example.com", "password123", "Test User", "Bio", "Joined", "image_url", "auth_id", "avatar")
        val requestBody = Json.encodeToString(userRequest)

        // Send a POST request to the /user/insert endpoint
        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }
    }
}


// /user/update/{id} endpoint
class UpdateUserTest {

    @Before
    fun setUp() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users)
        }
    }

    @Test
    fun testUserUpdate() = testApplication {
        application {
            module(true)
        }


        // Define the user data to insert
        val userInsert =
            UserInsert(true, "test@example.com", "password123", "Test User", "Bio", "Joined", "image_url", "auth_id", "avatar")
        val insertBody = Json.encodeToString(userInsert)

        // Send a POST request to the /user/insert endpoint
        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(insertBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // Define the user data to update
        val userUpdate =
            UserUpdate("test@example.com", "password123", "updated", "Bio", "Joined", "image_url", "auth_id", "avatar")
        val updateBody = Json.encodeToString(userUpdate)

        // Send a PATCH request to the /user/upate endpoint
        client.patch("/user/update/1") {
            contentType(ContentType.Application.Json)
            setBody(updateBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}


// /user/liked endpoint
class UserLikedTest {

    @Before
    fun setUp() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Trips)
            SchemaUtils.create(Liked)

        }
    }

    @Test
    fun testUserLiked() = testApplication {
        application {
            module(true)
        }


        // Define the user data to insert
        val userInsert =
            UserInsert(true, "test@example.com", "password123", "Test User", "Bio", "Joined", "image_url", "auth_id", "avatar")
        val insertBody = Json.encodeToString(userInsert)

        // Send a POST request to the /user/insert endpoint
        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(insertBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }


        val tripInsert = TripInsert(1, "cool trip", "cool description", "start", "end", 100, "image", "visited")
        val insertTripBody = Json.encodeToString(tripInsert)

        // Send a POST request to the /trip/insert endpoint
        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(insertTripBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
        }

        val likedInsert = LikedInsert(1, 1)
        val insertLikedBody = Json.encodeToString(likedInsert)

        // Send a POST request to the /trip/insert endpoint
        client.post("/user/liked") {
            contentType(ContentType.Application.Json)
            setBody(insertLikedBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}

// /user/disliked endpoint
class UserDislikedTest {

    @Before
    fun setUp() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Trips)
            SchemaUtils.create(Liked)

        }
    }

    @Test
    fun testUserDisliked() = testApplication {
        application {
            module(true)
        }


        // Define the user data to insert
        val userInsert =
            UserInsert(true, "test@example.com", "password123", "Test User", "Bio", "Joined", "image_url", "auth_id", "avatar")
        val insertBody = Json.encodeToString(userInsert)

        // Send a POST request to the /user/insert endpoint
        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(insertBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }


        val tripInsert = TripInsert(1, "cool trip", "cool description", "start", "end", 100, "image", "visited")
        val insertTripBody = Json.encodeToString(tripInsert)

        // Send a POST request to the /trip/insert endpoint
        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(insertTripBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
        }

        val likedInsert = LikedInsert(1, 1)
        val insertLikedBody = Json.encodeToString(likedInsert)

        // Send a POST request to the /trip/insert endpoint
        client.post("/user/liked") {
            contentType(ContentType.Application.Json)
            setBody(insertLikedBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
        }

        val dislikedInsert = LikedInsert(1, 1)
        val insertDislikedBody = Json.encodeToString(dislikedInsert)

        // Send a POST request to the /trip/insert endpoint
        client.delete("/user/disliked") {
            contentType(ContentType.Application.Json)
            setBody(insertDislikedBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}


// /user/{id}/friends
class UserFriendsTest {

    @Before
    fun setUp() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Friends)

        }
    }

    @Test
    fun testUserFriends() = testApplication {
        application {
            module(true)
        }


        // Define the user data to insert
        val userInsert1 =
            UserInsert(true, "test@example.com", "password123", "Test User", "Bio", "Joined", "image_url", "auth_id", "avatar")
        val insertBody1 = Json.encodeToString(userInsert1)

        // user1
        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(insertBody1)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // Define the user data to insert
        val userInsert2 =
            UserInsert(true, "test2@example.com", "password123", "Test User 2", "Bio", "Joined", "image_url", "auth_id", "avatar")
        val insertBody2 = Json.encodeToString(userInsert2)

        // user2
        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(insertBody2)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }


        val friendInsert = FriendInsert(1, 2)
        val updateBody = Json.encodeToString(friendInsert)

        // user1 and user2 become friends
        client.post("/friends/insert") {
            contentType(ContentType.Application.Json)
            setBody(updateBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
        }

        // call to the endpoint for user 1
        client.get("/user/1/friends").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                "[{\"id\":2,\"email\":\"test2@example.com\",\"password\":\"NO_RETURN_PASSWORD\"," +
                        "\"name\":\"Test User 2\",\"bio\":\"Bio\",\"joined\":\"Joined\"," +
                        "\"image\":\"image_url\",\"auth_id\":\"auth_id\",\"avatar\":\"avatar\"}]", bodyAsText()
            )
        }

        // for user 2
        client.get("/user/2/friends").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                "[{\"id\":1,\"email\":\"test@example.com\",\"password\":\"NO_RETURN_PASSWORD\"," +
                        "\"name\":\"Test User\",\"bio\":\"Bio\",\"joined\":\"Joined\"," +
                        "\"image\":\"image_url\",\"auth_id\":\"auth_id\",\"avatar\":\"avatar\"}]", bodyAsText()
            )
        }
    }
}


// /user/{id}/pins
class UserPinsTest {

    @Before
    fun setUp() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Pins)
        }
    }

    @Test
    fun testUserPins() = testApplication {
        application {
            module(true)
        }


        // Define the user data to insert
        val userInsert =
            UserInsert(true, "test@example.com", "password123", "Test User", "Bio", "Joined", "image_url", "auth_id", "avatar")
        val insertBody = Json.encodeToString(userInsert)

        // Send a POST request to the /user/insert endpoint
        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(insertBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // insert a trip for the associated pin first
        val tripInsert = TripInsert(1, "name", "description", "starttime", "endtime", 1, "image", "status")
        val tripinsertBody = Json.encodeToString(tripInsert)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripinsertBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }


        // insert a pin
        val pinInsert = PinInsert(1, 1, "city", "location", "description", "image")
        val pininsertBody = Json.encodeToString(pinInsert)

        client.post("/pin/insert") {
            contentType(ContentType.Application.Json)
            setBody(pininsertBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
        }

        // test endpoint
        client.get("/user/1/pins").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                "[{\"id\":1,\"userId\":1,\"tripID\":1,\"city\":\"city\",\"location\":\"location\"," +
                        "\"description\":\"description\",\"image\":\"image\"}]", bodyAsText()
            )
        }

    }
}


// /user/{id}/trips
class UserTripsTest {

    @Before
    fun setUp() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Trips)
        }
    }

    @Test
    fun testUserTrips() = testApplication {
        application {
            module(true)
        }

        // Define the user data to insert
        val userInsert =
            UserInsert(true, "test@example.com", "password123", "Test User", "Bio", "Joined", "image_url", "auth_id", "avatar")
        val insertBody = Json.encodeToString(userInsert)

        // Send a POST request to the /user/insert endpoint
        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(insertBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // add trip
        val tripInsert = TripInsert(1, "name", "description", "starttime", "endtime", 1, "image", "status")
        val tripinsertBody = Json.encodeToString(tripInsert)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripinsertBody)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // test endpoint
        client.get("/user/1/trips").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                "[{\"id\":1,\"userId\":1,\"name\":\"name\",\"description\":\"description\"," +
                        "\"startTime\":\"starttime\",\"endTime\":\"endtime\",\"score\":1,\"image\":\"image\"," +
                        "\"status\":\"status\"}]", bodyAsText()
            )
        }
    }
}


// /user/{userid}/trips/{tripid}/pins
class UserTripPinsTest {

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
    fun testUserTripPins() = testApplication {
        application {
            module(true)
        }

        val userInsert =
            UserInsert(true, "test@example.com", "password123", "Test User", "Bio", "Joined", "image_url", "auth_id", "avatar")
        val insertBody = Json.encodeToString(userInsert)

        // Send a POST request to the /user/insert endpoint
        client.post("/user/insert") {
            contentType(ContentType.Application.Json)
            setBody(insertBody)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User inserted successfully!", bodyAsText())
        }

        // we want to get pins of this trip
        val tripInsert1 = TripInsert(1, "right", "description", "starttime", "endtime", 1, "image", "status")
        val tripinsertBody1 = Json.encodeToString(tripInsert1)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripinsertBody1)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // decoy trip, we don't want pins from this one
        val tripInsert2 = TripInsert(1, "wrong", "description", "starttime", "endtime", 1, "image", "status")
        val tripinsertBody2 = Json.encodeToString(tripInsert2)

        client.post("/trip/insert") {
            contentType(ContentType.Application.Json)
            setBody(tripinsertBody2)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        // insert a pin to the right one
        val pinInsert1 = PinInsert(1, 1, "right city", "right location", "right description", "right image")
        val pininsertBody1 = Json.encodeToString(pinInsert1)

        client.post("/pin/insert") {
            contentType(ContentType.Application.Json)
            setBody(pininsertBody1)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
        }

        // insert a pin to the wrong one
        val pinInsert2 = PinInsert(1, 2, "wrong city", "wrong location", "wrong description", "wrong image")
        val pininsertBody2 = Json.encodeToString(pinInsert2)

        client.post("/pin/insert") {
            contentType(ContentType.Application.Json)
            setBody(pininsertBody2)
        }.apply {
            // Check the response status and body
            assertEquals(HttpStatusCode.OK, status)
        }

        // test endpoint
        client.get("/user/1/trips/1/pins").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                "[{\"id\":1,\"userId\":1,\"tripID\":1,\"city\":\"right city\"," +
                        "\"location\":\"right location\",\"description\":\"right description\"," +
                        "\"image\":\"right image\"}]", bodyAsText()
            )
        }
    }
}