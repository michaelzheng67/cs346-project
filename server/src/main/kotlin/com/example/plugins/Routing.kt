package com.example.plugins

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import net.codebot.models.*
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction


// Table schema for Users
object Users : IntIdTable("users") {
    val email = varchar("email", 255)
    val password = varchar("password", 50)
    val name = varchar("name", 255)
    val bio = varchar("bio", 1000)
    val joined = varchar("joined", 50)
    val image = text("image")
    val auth_id = text("auth_id")
    val avatar = text("avatar")
}

// Table schema for Pins
object Pins : IntIdTable("pins") {
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val tripId = integer("trip_id").references(Trips.id, onDelete = ReferenceOption.CASCADE)
    val city = varchar("city", 255).nullable()
    val image = text("image").nullable()
    val location = varchar("location", 255).nullable()
    val description = varchar("description", 255).nullable()
}

// Table schema for Trips
object Trips : IntIdTable("trips") {
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val description = varchar("description", 1000)
    val startTime = varchar("starttime", 50)
    val endTime = varchar("endtime", 50)
    val score = integer("score")
    val image = text("image")
    val status = varchar("status", 20)
}

object Friends : Table("friends") {
    val user1Id = integer("user1_id")
    val user2Id = integer("user2_id")
    override val primaryKey = PrimaryKey(user1Id, user2Id)
}

object Liked : Table("users_and_trips") {
    val userId = integer("user_id")
    val tripId = integer("trip_id")
    override val primaryKey = PrimaryKey(userId, tripId)
}


fun Application.configureRouting(testing: Boolean) {
    routing {
        get("/") {
            call.respondText("Received")
        }

        get("/user/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid number provided")
                return@get
            }

            val user = getUser(id)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else {
                call.respond(user)
            }
        }

        get("/user/exists/{id}") {
            val id = call.parameters["id"].toString()

            val exists = userExists(id)
            call.respond(exists)
        }

        get("/user/exchange_id/{auth_id}") {
            val auth_id = call.parameters["auth_id"]

            if (auth_id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid Auth ID provided")
                return@get
            }

            val user_id = getUserId(auth_id)
            if (user_id == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else {
                call.respond(user_id)
            }
        }

        // POST endpoint for creating users
        post("/user/insert") {
            val userRequest = call.receive<UserInsert>()

            insertUser(
                userRequest.testing,
                userRequest.email,
                userRequest.password,
                userRequest.name,
                userRequest.bio,
                userRequest.joined,
                userRequest.image,
                userRequest.auth_id,
                userRequest.avatar
            )



            call.respond(HttpStatusCode.OK, "User inserted successfully!")
        }

        patch("/user/update/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid number provided")
                return@patch
            }

            val userUpdate = call.receive<UserUpdate>()
            val updatedUser = updateUser(id, userUpdate)
            if (updatedUser == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else {
                call.respond(HttpStatusCode.OK, updatedUser)
            }
        }

        post("/user/liked") {
            val liked = call.receive<LikedInsert>()

            insertLiked(liked.userId, liked.tripId)

            // Call gpt to generate new avatar for given user asynchronously
            if (!testing) {
                launch {
                    val result = gptProfiler(liked.userId) ?: ""
                    updateUserAvatar(liked.userId, result)
                }
            }


            call.respond(HttpStatusCode.OK, "Liked trip successfully!")
        }

        delete("/user/disliked") {
            // taking the same form of input as "userliked" endpoint
            val disliked = call.receive<LikedInsert>()
            deleteLikedEntry(disliked.userId, disliked.tripId)

            // Call gpt to generate new avatar for given user asynchronously
            if (!testing) {
                launch {
                    val result = gptProfiler(disliked.userId) ?: ""
                    updateUserAvatar(disliked.userId, result)
                }
            }


            call.respond(HttpStatusCode.OK, "Disliked trip successfully!")

        }


        // POST endpoint for creating pins
        post("/pin/insert") {
            val pinRequest = call.receive<PinInsert>()

            insertPin(
                pinRequest.userId,
                pinRequest.tripID,
                pinRequest.city,
                pinRequest.location,
                pinRequest.description,
                pinRequest.image
            )
            call.respond(HttpStatusCode.OK, "Pin inserted successfully!")
        }

        // POST endpoint for creating trips
        post("/trip/insert") {
            val tripRequest = call.receive<TripInsert>()

            val id = insertTrip(
                tripRequest.userId,
                tripRequest.name,
                tripRequest.description,
                tripRequest.startTime,
                tripRequest.endTime,
                tripRequest.score,
                tripRequest.image,
                tripRequest.status
            )

            // Call gpt to generate new avatar for given user asynchronously
            if (!testing) {
                launch {
                    val result = gptProfiler(tripRequest.userId) ?: ""
                    updateUserAvatar(tripRequest.userId, result)
                }
            }



            call.respond(id.toString())
        }

        post("/friends/insert") {
            val friendRequest = call.receive<FriendInsert>()

            //Double insert to make get request easier
            insertFriend(friendRequest.user1Id, friendRequest.user2Id)
            insertFriend(friendRequest.user2Id, friendRequest.user1Id)

            call.respond(HttpStatusCode.OK, "Friends inserted successfully!")
        }

        post("/friends/remove") {
            val friendRequest = call.receive<FriendInsert>()

            //Every friendship between to users (their ids) will appear as two entries in the DB
            //To get user [ID]'s friends, we simply get the second attribute of every row who's first attribute is [ID]
            //This probably isn't the cleanest way to do it, but INTs don't use up much space, so we don't lose much
            deleteFriend(friendRequest.user1Id, friendRequest.user2Id)
            deleteFriend(friendRequest.user2Id, friendRequest.user1Id)

            call.respond(HttpStatusCode.OK, "Friends removed successfully!")
        }

        /*
        This get endpoint returns the list of user_ids that are friends with given user
        Might want to change and return User objects directly
         */
        get("/user/{id}/friends") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                return@get
            }

            val listFriends = getFriends(id)
            listFriends.forEach { friend -> println("Friend : $friend") }

            call.respond(listFriends)
        }


        get("/user/{id}/likedTrips") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                return@get
            }

            val likedTrips = getLiked(id)

            call.respond(likedTrips)
        }


        // GET endpoint for pins given a user id
        get("/user/{id}/pins") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                return@get
            }

            val pins = getPinsByUserId(id)

            call.respond(pins)
        }

        get("/user/{id}/trips") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                return@get
            }


            val listTrips = getTrips(id)

            call.respond(listTrips)
        }

        get("/user/{user_id}/trips/{trip_id}/pins") {
            val userId = call.parameters["user_id"]?.toIntOrNull()
            val tripId = call.parameters["trip_id"]?.toIntOrNull()

            if (userId == null || tripId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                return@get
            }


            val listPins = getTripPins(userId, tripId)
            listPins.forEach { pin -> println("Pin : $pin") }

            call.respond(listPins)
        }

        get("/trips") {
            val trips = getAllTrips()
            call.respond(trips)
        }

        delete("/trips/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid number provided")
                return@delete
            }

            deletePinsByTripId(id)
            deleteTrip(id)
            deletePinsByTripId(id)
            call.respond(HttpStatusCode.OK, "Trip deleted successfully!")

        }

        post("/trips/data-check") {
            val tripIDs = call.receive<List<Int>>()

            val backendIDs = transaction {
                Trips.slice(Trips.id).selectAll().toList().map { it[Trips.id].value }

            }

            call.respond(HttpStatusCode.OK, tripIDs.containsAll(backendIDs))
        }


        get("/trip/{id}/pins") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid number provided")
                return@get
            }

            val trips = getPinsByTripId(id)
            call.respond(trips)
        }

        delete("/trip/{id}/pins-delete") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid number provided")
                return@delete
            }

            deletePinsByTripId(id)
            call.respond(HttpStatusCode.OK, "Pins deleted from Trip successfully!")
        }


        get("/trip/top/{n}") {
            val n = call.parameters["n"]?.toIntOrNull()
            if (n == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid number provided")
                return@get
            }

            val topTrips = getTopTrips(n)
            call.respond(topTrips)
        }

        patch("/trip/update/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid number provided")
                return@patch
            }

            val tripUpdate = call.receive<TripUpdate>()
            val updatedTrip = updateTrip(id, tripUpdate)
            if (updatedTrip == null) {
                call.respond(HttpStatusCode.NotFound, "Trip not found")
            } else {
                call.respond(HttpStatusCode.OK, updatedTrip)
            }
        }

        get("/ai/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid number provided")
                return@get
            }

            // get all trips that user has been on
            val listTrips = getTrips(id)

            // get all trips that a user liked
            val likedTrips = getLiked(id)

            // concatenate trips together
            val allTrips = listTrips + likedTrips

            // Check if allTrips is empty, return a default string
            if (allTrips.isEmpty()) {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "role" to "",
                        "content" to "You haven't been on any trips yet. How about planning a new adventure?",
                        "name" to null,
                        "function_call" to null
                    )
                )
                return@get
            }

            val names = allTrips.map { it.name }
            val namesString = names.joinToString(separator = ", ", prefix = "", postfix = ".")


            // fetch from openai
            val dotenv = dotenv {
                ignoreIfMissing = true
            }

            val openAI = OpenAI(token = dotenv["OPEN_API_KEY"], logging = LoggingConfig(LogLevel.All))

            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-3.5-turbo"),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = "You are a travel planner that gives a city suggestion that exists in the world and describes it in 150 characters or less."
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        content = "I've been to $namesString Where should I visit next?"
                    )
                )
            )

            val response = openAI.chatCompletion(chatCompletionRequest).choices[0].message
            // return summary + list of trip objects
            call.respond(HttpStatusCode.OK, response)
        }

        get("/ai/profiler/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid number provided")
                return@get
            }

            // get users avatar string
            val response = getUserAvatar(id) ?: ""
            // return summary + list of trip objects
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
