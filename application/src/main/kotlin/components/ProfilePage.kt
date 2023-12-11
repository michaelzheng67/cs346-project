package components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import data.pinsOf
import net.codebot.models.Pin
import net.codebot.models.Trip
import net.codebot.models.User

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfilePage(
    user: User,
    allTripList: MutableState<MutableList<Trip>>,
    selectedTab: MutableState<String>,
    tripToEdit: MutableState<Trip>,
    currentUserId: Int,
    likedTripsIDs: MutableState<MutableList<Int>>,
    friends: MutableState<List<User>>,
    viewTrip: MutableState<Boolean>
) {
    val dummyTrip = Trip(100, 100, "str", "str", "str", "str", 10, "str", "str")
    val selectedTrip = remember { mutableStateOf(dummyTrip) }
    val tripList = allTripList.value.filter { it.userId == user.id }
    val selectedTripPins = remember { mutableStateOf(mutableListOf<Pin>()) }

    // map avatar string -> avatar image
    val imageMap = hashMapOf(
        "CitySlicker" to "cityslicker.png",
        "Islander" to "islander.png",
        "Foodie" to "foodie.png",
        "Journeyman" to "journeyman.png",
        "Thrillseeker" to "thrillseeker.png"
    )

    val descriptionMap = hashMapOf(
        "CitySlicker" to "They love exploring different cities and cultures.",
        "Islander" to "They love the tropics and islands.",
        "Foodie" to "They love exploring different eateries.",
        "Journeyman" to "They love going to the countryside.",
        "Thrillseeker" to "They love seeking new thrills."
    )


    Row(modifier = Modifier.padding(10.dp)) {
        Column(modifier = Modifier.width(200.dp).verticalScroll(rememberScrollState())) {
            ProfilePicture(user.image, false, 200.dp)
            Row {
                Text(user.name)
                Spacer(modifier = Modifier.width(5.dp))
                Text("id#${user.id}")
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text("Member Since" + " " + user.joined)
            Spacer(modifier = Modifier.height(5.dp))
            Text(user.bio)

            Spacer(modifier = Modifier.height(30.dp))

            // only load if there is avatar specified
            if (imageMap.contains(user.avatar)) {
                Text("Traveller Type:")
                Text(user.avatar)
                Column() {
                    TooltipArea({
                        Text(
                            descriptionMap[user.avatar] ?: "No avatar found",
                            color = Color.White,
                            modifier = Modifier.background(Color.Gray)
                        )
                    }) {
                        Image(
                            painter = painterResource(imageMap[user.avatar] ?: "placeholderPFP.png"),
                            contentDescription = null,
                            modifier = Modifier.height(100.dp)
                        )
                    }
                }
            }

        }
        Spacer(modifier = Modifier.width(10.dp))
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(300.dp),
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f),
            content = {
                items(tripList) { trip ->
                    val liked = remember { mutableStateOf(likedTripsIDs.value.contains(trip.id)) }
                    liked.value = likedTripsIDs.value.contains(trip.id)
                    val score = remember { mutableStateOf(trip.score) }
                    score.value = trip.score
                    TripButton(
                        trip = trip, onclick =
                        {
                            viewTrip.value = true
                            selectedTrip.value = trip
                            selectedTripPins.value = pinsOf(trip.id)
                        }, currentUserId, selectedTab, tripToEdit, liked, score,
                        {
                            likedTripsIDs.value.add(trip.id)
                            val index = allTripList.value.indexOfFirst { it.id == trip.id }
                            allTripList.value[index] = allTripList.value[index].copy(score = score.value)
                        },
                        {
                            likedTripsIDs.value.remove(trip.id)
                            val index = allTripList.value.indexOfFirst { it.id == trip.id }
                            allTripList.value[index] = allTripList.value[index].copy(score = score.value)
                        }
                    )
                }
            },
        )
        Spacer(modifier = Modifier.width(10.dp))
        if (viewTrip.value) {
            Column(modifier = Modifier.border(BorderStroke(1.dp, Color.LightGray)).fillMaxHeight().width(280.dp)) {
                Column(modifier = Modifier.padding(5.dp)) {
                    TripSelectedView(
                        selectedTrip,
                        selectedTripPins,
                        currentUserId,
                        selectedTab,
                        tripToEdit,
                        friends,
                        {
                            viewTrip.value = false
                            val index = allTripList.value.indexOfFirst { it.id == selectedTrip.value.id }
                            allTripList.value.removeAt(index)
                        })
                }
            }
        }
    }
}