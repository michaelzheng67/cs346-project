package data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.codebot.models.*
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter


/**
 * Returns the given User's friends
 *
 * @param userId id of given user
 * @return List<User> returns friends as List<User> (where User can be serialized)
 */
fun friendsOf(userId: Int): List<User> {

    val response = URL("https://journeymid.ue.r.appspot.com/user/$userId/friends").readText()
    val friends = Json.decodeFromString<List<User>>(response)

    return friends
}

fun insertFriends(friendInsert: FriendInsert): String {
    if (friendInsert.user1Id == friendInsert.user2Id) {
        return "Can't befriend yourself"
    }

    val friend = Json.encodeToString(friendInsert)

    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://journeymid.ue.r.appspot.com/friends/insert"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(friend))
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun removeFriends(friendInsert: FriendInsert): String {
    val friend = Json.encodeToString(friendInsert)

    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://journeymid.ue.r.appspot.com/friends/remove"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(friend))
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun pinsOf(tripId: Int): MutableList<Pin> {

    val response = URL("https://journeymid.ue.r.appspot.com/trip/$tripId/pins").readText()
    val pinList = Json.decodeFromString<MutableList<Pin>>(response)

    return pinList
}

fun allTrips(): List<Trip> {
    val response = URL("https://journeymid.ue.r.appspot.com/trips").readText()
    val tripList = Json.decodeFromString<List<Trip>>(response)

    return tripList
}

//Returns true if there are new trips to load, false otherwise
//We're doing a POST request to be able to send over the IDs in the body
//https://stackoverflow.com/questions/978061/http-get-with-request-body
//https://stackoverflow.com/questions/19637459/rest-api-using-post-instead-of-get
fun askNewTrips(tripIDs: List<Int>): Boolean {
    val tripIDs2 = Json.encodeToString(tripIDs)

    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://journeymid.ue.r.appspot.com/trips/data-check"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(tripIDs2))
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return Json.decodeFromString<Boolean>(response.body())
}


fun tripsOf(userId: Int): List<Trip> {
    val response = URL("https://journeymid.ue.r.appspot.com/user/$userId/trips").readText()
    val tripList = Json.decodeFromString<List<Trip>>(response)

    return tripList
}

fun addTrip(tripInsert: TripInsert): String {
    val trip = Json.encodeToString(tripInsert)

    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://journeymid.ue.r.appspot.com/trip/insert"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(trip))
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun editTrip(tripUpdate: TripUpdate, tripId: Int?): String {
    val trip = Json.encodeToString(tripUpdate)

    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://journeymid.ue.r.appspot.com/trip/update/$tripId"))
        .method("PATCH", HttpRequest.BodyPublishers.ofString(trip))
        .header("Content-Type", "application/json")
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun deletePins(tripId: Int): String {

    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://journeymid.ue.r.appspot.com/trip/$tripId/pins-delete"))
        .DELETE()
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun addPin(pinInsert: PinInsert): String {
    val trip = Json.encodeToString(pinInsert)

    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://journeymid.ue.r.appspot.com/pin/insert"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(trip))
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun likedTrips(userId: Int): List<Trip> {
    val response = URL("https://journeymid.ue.r.appspot.com/user/$userId/likedTrips").readText()
    val likedTrips = Json.decodeFromString<List<Trip>>(response)

    return likedTrips
}

fun editUser(userUpdate: UserUpdate, userId: Int): String {
    val user = Json.encodeToString(userUpdate)

    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://journeymid.ue.r.appspot.com/user/update/$userId"))
        .method("PATCH", HttpRequest.BodyPublishers.ofString(user))
        .header("Content-Type", "application/json")
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun userAvatar(userId: Int): String {
    val response = URL("https://journeymid.ue.r.appspot.com/ai/profiler/$userId").readText()
//    val apiResponse = Json.decodeFromString<Avatar>(response)

    return response
}

fun userIdFromAuthId(authId: String): Int {
    val response = URL("https://journeymid.ue.r.appspot.com/user/exchange_id/$authId").readText()
    return response.toInt()
}

fun userExists(authId: String): Boolean {
    val response = URL("https://journeymid.ue.r.appspot.com/user/exists/$authId").readText()
    return response.toBoolean()
}

fun addUser(authId: String, name: String): String {
    val dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val localDate = LocalDate.now()
    val currentDate = dtf.format(localDate)
    val userInsert =
        UserInsert(false, "email@email.com", "password", name, "No bio yet", currentDate, "image", authId, "avatar")
    val user = Json.encodeToString(userInsert)

    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://journeymid.ue.r.appspot.com/user/insert"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(user))
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun getUserById(userId: Int): User {
    val response = URL("https://journeymid.ue.r.appspot.com/user/$userId").readText()
    val thisUser = Json.decodeFromString<User>(response)
    return thisUser
}

fun getGptSuggestions(userId: Int): String {
    val response = URL("https://journeymid.ue.r.appspot.com/ai/$userId").readText()
    val suggestions = Json.decodeFromString<Avatar>(response)
    return suggestions.content
}

fun likeTrip(userId: Int, tripId: Int) {
    val likeInfo = Json.encodeToString(LikedInsert(userId, tripId))

    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://journeymid.ue.r.appspot.com/user/liked"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(likeInfo))
        .build()

    client.send(request, HttpResponse.BodyHandlers.ofString())
}

fun unlikeTrip(userId: Int, tripId: Int) {
    val likeInfo = Json.encodeToString(LikedInsert(userId, tripId))

    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://journeymid.ue.r.appspot.com/user/disliked"))
        .header("Content-Type", "application/json")
        .method("DELETE", HttpRequest.BodyPublishers.ofString(likeInfo))
        .build()

    client.send(request, HttpResponse.BodyHandlers.ofString())
}

fun getNTrips(n: Int): List<Trip> {
    val response = URL("https://journeymid.ue.r.appspot.com/trip/top/$n").readText()
    val tripList = Json.decodeFromString<List<Trip>>(response)
    return tripList
}

fun deleteTrip(tripId: Int) {
    print(tripId.toString())
    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .DELETE()
        .uri(URI.create("https://journeymid.ue.r.appspot.com/trips/${tripId}"))
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    print(response)
}