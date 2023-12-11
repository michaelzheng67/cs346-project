package com.example.plugins

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import io.github.cdimascio.dotenv.dotenv
import net.codebot.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction


// user functions
fun getUser(id: Int): User? {
    return transaction {
        Users.select { Users.id eq id }
            .mapNotNull { toUser(it) }
            .singleOrNull()
    }
}

fun getUserId(auth_id: String): Int? {
    return transaction {
        Users.select { Users.auth_id eq auth_id }
            .mapNotNull { it[Users.id].value }
            .singleOrNull()
    }
}

fun updateUserAvatar(userId: Int, newAvatar: String) {
    transaction {
        Users.update({ Users.id eq userId }) {
            it[avatar] = newAvatar
        }
    }
}

fun insertUser(
    testing: Boolean,
    email: String,
    password: String,
    name: String,
    bio: String,
    joined: String,
    image: String,
    auth_id: String,
    avatar: String
) {
    transaction {
        addLogger(StdOutSqlLogger)

        val userId = Users.insert {
            it[Users.email] = email
            it[Users.password] = password
            it[Users.name] = name
            it[Users.bio] = bio
            it[Users.joined] = joined
            it[Users.image] = image
            it[Users.auth_id] = auth_id
            it[Users.avatar] = avatar
        } get Users.id
    }
}

fun updateUser(userId: Int, userUpdate: UserUpdate): User? {
    return transaction {
        Users.select { Users.id eq userId }
            .firstOrNull()
            ?.let {
                Users.update({ Users.id eq userId }) {
                    userUpdate.email?.let { email -> it[Users.email] = email }
                    userUpdate.password?.let { password -> it[Users.password] = password }
                    userUpdate.name?.let { name -> it[Users.name] = name }
                    userUpdate.bio?.let { bio -> it[Users.bio] = bio }
                    userUpdate.joined?.let { joined -> it[Users.joined] = joined }
                    userUpdate.image?.let { image -> it[Users.image] = image }
                    userUpdate.auth_id?.let { auth_id -> it[Users.auth_id] = auth_id }
                    userUpdate.avatar?.let { avatar -> it[Users.avatar] = avatar }
                }
                toUser(Users.select { Users.id eq userId }.first())
            }
    }
}

fun getUserAvatar(userId: Int): String? {
    return transaction {
        // Select the avatar column where the user ID matches
        Users.select { Users.id eq userId }
            .mapNotNull { it[Users.avatar] }
            .singleOrNull()  // Returns null if the user is not found
    }
}

fun getPinsByUserId(userId: Int): List<Pin> {
    return transaction {
        Pins.select { Pins.userId eq userId }.toList().map {
            Pin(
                id = it[Pins.id].value,
                userId = it[Pins.userId],
                tripID = it[Pins.tripId],
                city = it[Pins.city].orEmpty(),
                image = it[Pins.image].orEmpty(),
                location = it[Pins.location].orEmpty(),
                description = it[Pins.description].orEmpty(),
            )
        }
    }
}

fun insertFriend(user1Id: Int, user2Id: Int) {
    transaction {
        addLogger(StdOutSqlLogger)

        val id = Friends.insert {
            it[Friends.user1Id] = user1Id
            it[Friends.user2Id] = user2Id
        } get Friends.user1Id
    }
}

fun deleteFriend(user1Id: Int, user2Id: Int) {
    transaction {
        addLogger(StdOutSqlLogger)

        Friends.deleteWhere { (Friends.user1Id eq user1Id) and (Friends.user2Id eq user2Id) }
    }
}

fun deleteTrip(id: Int) {
    transaction {
        addLogger(StdOutSqlLogger)

        Trips.deleteWhere { (Trips.id eq id) }
    }
}


fun toUser(row: ResultRow): User = User(
    id = row[Users.id].value,
    email = row[Users.email],
    password = row[Users.password],
    name = row[Users.name],
    bio = row[Users.bio],
    joined = row[Users.joined],
    image = row[Users.image],
    auth_id = row[Users.auth_id],
    avatar = row[Users.avatar]
)


// pin functions
fun insertPin(userId: Int, tripId: Int, city: String, location: String, description: String, image: String) {
    transaction {
        // If you want to see SQL logs:
        addLogger(StdOutSqlLogger)

        val pinId = Pins.insert {
            it[Pins.userId] = userId
            it[Pins.tripId] = tripId
            it[Pins.city] = city
            it[Pins.location] = location
            it[Pins.description] = description
            it[Pins.image] = image
        } get Pins.id
    }
}

fun getPinsByTripId(tripId: Int): List<Pin> {
    return transaction {
        Pins.select { Pins.tripId eq tripId }.toList().map {
            Pin(
                id = it[Pins.id].value,
                userId = it[Pins.userId],
                tripID = it[Pins.tripId],
                city = it[Pins.city].orEmpty(),
                image = it[Pins.image].orEmpty(),
                location = it[Pins.location].orEmpty(),
                description = it[Pins.description].orEmpty(),
            )
        }
    }
}

fun deletePinsByTripId(tripId: Int) {
    transaction {
        // Delete entries from Pins where tripId matches
        Pins.deleteWhere { Pins.tripId eq tripId }
    }
}


// trips functions
fun insertTrip(
    userId: Int,
    name: String,
    description: String,
    startTime: String,
    endTime: String,
    score: Int,
    image: String,
    status: String
): Int {
    var returnId = 0
    transaction {
        addLogger(StdOutSqlLogger)

        val tripId = Trips.insert {
            it[Trips.userId] = userId
            it[Trips.name] = name
            it[Trips.description] = description
            it[Trips.startTime] = startTime
            it[Trips.endTime] = endTime
            it[Trips.score] = score
            it[Trips.image] = image
            it[Trips.status] = status
        } get Trips.id
        returnId = tripId.value
    }
    return (returnId)
}

fun insertLiked(userId: Int, tripId: Int) {
    transaction {
        addLogger(StdOutSqlLogger)

        val id = Liked.insert {
            it[Liked.userId] = userId
            it[Liked.tripId] = tripId
        } get Liked.userId

        // Retrieve the current score and increment it
        val currentScore = Trips.slice(Trips.score)
            .select { Trips.id eq tripId }
            .single()[Trips.score]

        // Update the score in Trips table
        Trips.update({ Trips.id eq tripId }) {
            it[score] = currentScore + 1
        }
    }
}

fun deleteLikedEntry(userId: Int, tripId: Int) {
    transaction {
        addLogger(StdOutSqlLogger)

        Liked.deleteWhere {
            (Liked.userId eq userId) and (Liked.tripId eq tripId)
        }

        // Retrieve the current score and increment it
        val currentScore = Trips.slice(Trips.score)
            .select { Trips.id eq tripId }
            .single()[Trips.score]

        // Update the score in Trips table
        Trips.update({ Trips.id eq tripId }) {
            it[score] = currentScore - 1
        }
    }
}

fun getLiked(uesrId: Int): List<Trip> {
    return transaction {
        Liked.join(Trips, JoinType.INNER, additionalConstraint = { Liked.tripId eq Trips.id })
            .slice(
                Trips.id,
                Trips.userId,
                Trips.name,
                Trips.description,
                Trips.startTime,
                Trips.endTime,
                Trips.score,
                Trips.image,
                Trips.status
            )
            .select { Liked.userId eq uesrId }.toList().map { toTrip(it) }
    }
}

fun getFriends(userId: Int): List<User> {
    var friends = emptyList<User>()
    transaction {
        addLogger(StdOutSqlLogger)

        friends = Friends.join(Users, JoinType.INNER, additionalConstraint = { Users.id eq Friends.user2Id })
            .slice(
                Users.id,
                Users.email,
                Users.password,
                Users.name,
                Users.bio,
                Users.joined,
                Users.image,
                Users.auth_id,
                Users.avatar
            )
            .select { Friends.user1Id eq userId }
            .toList().map {
                User(
                    it[Users.id].value,
                    it[Users.email],
                    "NO_RETURN_PASSWORD",
                    it[Users.name],
                    it[Users.bio],
                    it[Users.joined],
                    it[Users.image],
                    it[Users.auth_id],
                    it[Users.avatar]
                )
            }
    }
    return friends
}

fun getAllTrips(): List<Trip> {
    var trips = emptyList<Trip>()
    transaction {
        addLogger(StdOutSqlLogger)

        trips = Trips
            .selectAll()
            .map {
                toTrip(it)
            }
    }
    return trips
}

fun getTrips(userId: Int): List<Trip> {
    var trips = emptyList<Trip>()
    transaction {
        addLogger(StdOutSqlLogger)

        trips = Trips
            .slice(
                Trips.id,
                Trips.userId,
                Trips.name,
                Trips.description,
                Trips.startTime,
                Trips.endTime,
                Trips.score,
                Trips.image,
                Trips.status
            )
            .select { (Trips.userId eq userId) }
            .toList()
            .map {
                Trip(
                    it[Trips.id].value,
                    it[Trips.userId],
                    it[Trips.name],
                    it[Trips.description],
                    it[Trips.startTime],
                    it[Trips.endTime],
                    it[Trips.score],
                    it[Trips.image],
                    it[Trips.status]
                )
            }
    }
    return trips
}

fun getTopTrips(n: Int): List<Trip> {
    return transaction {
        Trips.selectAll()
            .orderBy(Trips.score to SortOrder.DESC)
            .limit(n)
            .map { toTrip(it) }
    }
}

fun updateTrip(tripId: Int, tripUpdate: TripUpdate): Trip? {
    return transaction {
        Trips.select { Trips.id eq tripId }
            .firstOrNull()
            ?.let {
                Trips.update({ Trips.id eq tripId }) {
                    tripUpdate.userId?.let { userId -> it[Trips.userId] = userId }
                    tripUpdate.name?.let { name -> it[Trips.name] = name }
                    tripUpdate.description?.let { description -> it[Trips.description] = description }
                    tripUpdate.startTime?.let { startTime -> it[Trips.startTime] = startTime }
                    tripUpdate.endTime?.let { endTime -> it[Trips.endTime] = endTime }
                    tripUpdate.score?.let { score -> it[Trips.score] = score }
                    tripUpdate.image?.let { image -> it[Trips.image] = image }
                    tripUpdate.status?.let { status -> it[Trips.status] = status }
                }
                toTrip(Trips.select { Trips.id eq tripId }.first())
            }
    }
}

fun toTrip(row: ResultRow): Trip = Trip(
    id = row[Trips.id].value,
    userId = row[Trips.userId],
    name = row[Trips.name],
    description = row[Trips.description],
    startTime = row[Trips.startTime],
    endTime = row[Trips.endTime],
    score = row[Trips.score],
    image = row[Trips.image],
    status = row[Trips.status]
)

fun getTripPins(userId: Int, tripId: Int): List<Pin> {
    var pins = emptyList<Pin>()

    transaction {
        addLogger(StdOutSqlLogger)

        pins = Pins
            .select { (Pins.tripId eq tripId) and (Pins.userId eq userId) }
            .toList()
            .map {
                Pin(
                    it[Pins.id].value,
                    it[Pins.userId],
                    it[Pins.tripId],
                    it[Pins.city].orEmpty(),
                    it[Pins.location].orEmpty(),
                    it[Pins.description].orEmpty(),
                    it[Pins.image].orEmpty()
                )
            }
    }
    return pins
}

fun userExists(authId: String): Boolean {
    val t = transaction {
        Users.select { Users.auth_id eq authId }
            .mapNotNull { toUser(it) }
            .singleOrNull()
    }
    return (t != null)
}

suspend fun gptProfiler(id: Int): String? {

    // get all trips that user has been on
    val listTrips = getTrips(id)

    // get all trips that a user liked
    val likedTrips = getLiked(id)

    // concatenate trips together
    val allTrips = listTrips + likedTrips
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
                content = "You can only respond with just one word, and it must be a type. There are 5 types of people based on where they've visited: " +
                        "- CitySlicker (been to multiple major cities)\n" +
                        "- Islander (been to multiple islands)\n" +
                        "- Foodie (been to multiple restaurants)\n" +
                        "- Journeyman (been to multiple rural areas)\n" +
                        "- Thrillseeker (been to multiple adventure areas)"
            ),
            ChatMessage(
                role = ChatRole.User,
                content = "I've been to $namesString What type of person am I? Your response must be just one word"
            )
        )
    )

    val response = openAI.chatCompletion(chatCompletionRequest).choices[0].message
    return response.content
}