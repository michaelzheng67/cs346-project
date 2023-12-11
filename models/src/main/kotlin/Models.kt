package net.codebot.models

import kotlinx.serialization.Serializable

// This is an example definition of a data class
//@Serializable
//data class sample(
//    val id: Int,
//    val title: String? = null,
//    val content: String? = null
//)

// Data class for users
@Serializable
data class User(
    val id: Int,
    val email: String,
    val password: String,
    val name: String,
    val bio: String,
    val joined: String,
    val image: String,
    val auth_id: String,
    val avatar: String
)

@Serializable
data class UserInsert(
    val testing: Boolean,
    val email: String,
    val password: String,
    val name: String,
    val bio: String,
    val joined: String,
    val image: String,
    val auth_id: String,
    val avatar: String
)

@Serializable
data class UserUpdate(
    val email: String?,
    val password: String?,
    val name: String?,
    val bio: String?,
    val joined: String?,
    val image: String?,
    val auth_id: String?,
    val avatar: String
)


// data class for pins. Each pin is essentially a place associated with a user wrapped with user-specific info
@Serializable
data class Pin(
    val id: Int,
    val userId: Int,
    val tripID: Int,
    var city: String,
    var location: String,
    var description: String,
    var image: String,
)

@Serializable
data class PinInsert(
    val userId: Int,
    val tripID: Int,
    val city: String,
    val location: String,
    val description: String,
    val image: String,
)

// data class for trip. A trip is a collection of pins associated with a user
@Serializable
data class Trip(
    val id: Int,
    val userId: Int,
    val name: String,
    val description: String,
    val startTime: String,
    val endTime: String,
    var score: Int,
    val image: String,
    val status: String,
)

@Serializable
data class TripInsert(
    val userId: Int,
    val name: String,
    val description: String,
    val startTime: String,
    val endTime: String,
    val score: Int,
    val image: String,
    val status: String
)

@Serializable
data class TripUpdate(
    val userId: Int?,
    val name: String?,
    val description: String?,
    val startTime: String?,
    val endTime: String?,
    val score: Int?,
    val image: String?,
    val status: String?
)

@Serializable
data class FriendInsert(
    val user1Id: Int,
    val user2Id: Int,
)

@Serializable
data class LikedInsert(
    val userId: Int,
    val tripId: Int,
)


@Serializable
data class Location(
    val address1: String,
    val address2: String,
    val city: String,
    val zip_code: String,
    val country: String,
    val state: String,
    val display_phone: String
) {
    constructor() : this("", "", "", "", "", "", "")
}

@Serializable
data class Business(
    val name: String,
    val rating: Double,
    val price: String,
    val location: Location
) {
    constructor() : this("name", 0.0, "", Location())
}

@Serializable
data class YelpQueryInfo(
    val businesses: List<Business>,
    val total: Int,
) {
    constructor() : this(emptyList<Business>(), 0)
}


@Serializable
data class Avatar(
    val role: String?,
    val content: String,
    val name: String?,
    val function_call: String?,
)


@Serializable
data class WindowDetails(
    val width: Int,
    val height: Int,
    val x: Int,
    val y: Int,
)