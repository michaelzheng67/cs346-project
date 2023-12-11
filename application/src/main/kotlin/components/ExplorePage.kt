package components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import data.pinsOf
import net.codebot.models.Pin
import net.codebot.models.Trip
import net.codebot.models.User

@Composable
fun ExplorePage(
    operatingUser: Int,
    allTrips: MutableState<MutableList<Trip>>,
    likedTripsIDs: MutableState<MutableList<Int>>,
    friendsIDs: List<Int>,
    selectedTab: MutableState<String>,
    tripToEdit: MutableState<Trip>,
    friends: MutableState<List<User>>
) {

    val filteredTrips = remember { mutableStateOf(allTrips.value) }
    val viewTrip = remember { mutableStateOf(false) }
    val selectedTrip = remember { mutableStateOf(allTrips.value[0]) }
    val selectedTripPins = remember { mutableStateOf(mutableListOf<Pin>()) }

    Column(
        modifier = Modifier.fillMaxHeight().padding(10.dp)
    ) {
        Filters(updateFilteredTrips = {
            filteredTrips.value = filterTrips(allTrips.value, it, friendsIDs, likedTripsIDs.value)
        })

        Row {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(300.dp),
                verticalItemSpacing = 4.dp,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f),
                content = {
                    items(filteredTrips.value) { trip ->
                        val liked = remember { mutableStateOf(likedTripsIDs.value.contains(trip.id)) }
                        liked.value = likedTripsIDs.value.contains(trip.id)
                        val score = remember { mutableStateOf(trip.score) }
                        score.value = trip.score
                        TripButton(
                            trip = trip,
                            onclick = {
                                viewTrip.value = false
                                viewTrip.value = true
                                selectedTrip.value = trip
                                selectedTripPins.value = pinsOf(trip.id)
                            },
                            operatingUser, selectedTab, tripToEdit, liked, score,
                            onLike = {
                                likedTripsIDs.value.add(trip.id)
                                val index = allTrips.value.indexOfFirst { it.id == trip.id }
                                allTrips.value[index] = allTrips.value[index].copy(score = score.value)
                                val filterIndex = filteredTrips.value.indexOfFirst { it.id == trip.id }
                                filteredTrips.value[filterIndex] =
                                    filteredTrips.value[filterIndex].copy(score = score.value)
                            },
                            onDislike = {
                                likedTripsIDs.value.remove(trip.id)
                                val index = allTrips.value.indexOfFirst { it.id == trip.id }
                                allTrips.value[index] = allTrips.value[index].copy(score = score.value)
                                val filterIndex = filteredTrips.value.indexOfFirst { it.id == trip.id }
                                filteredTrips.value[filterIndex] =
                                    filteredTrips.value[filterIndex].copy(score = score.value)
                            }

                        )
                    }
                }
            )

            Spacer(modifier = Modifier.width(10.dp))
            if (viewTrip.value) {
                Column(
                    modifier = Modifier.border(BorderStroke(1.dp, Color.LightGray)).fillMaxHeight().width(280.dp)
                ) {
                    Column(modifier = Modifier.padding(5.dp)) {
                        TripSelectedView(
                            selectedTrip,
                            selectedTripPins,
                            operatingUser,
                            selectedTab,
                            tripToEdit,
                            friends, {
                                viewTrip.value = false
                                val index = allTrips.value.indexOfFirst { it.id == selectedTrip.value.id }
                                allTrips.value.removeAt(index)
                            }
                        )
                    }
                }
            }
        }
    }
}


fun filterTrips(
    trips: MutableList<Trip>,
    filters: Map<String, Boolean>,
    friendsIDs: List<Int>,
    likedTripsIDs: List<Int>
): MutableList<Trip> {
    return trips
        .filter { !filters.getOrDefault("Friends", false) || friendsIDs.contains(it.userId) }.toMutableList()
        .filter { !filters.getOrDefault("Liked", false) || likedTripsIDs.contains(it.id) }.toMutableList()
}

@Composable
fun Filters(updateFilteredTrips: (Map<String, Boolean>) -> Unit) {

    val checkedBoxes = remember { mutableMapOf("Friends" to false, "Liked" to false) }
    val friendsFilter = remember { mutableStateOf(false) }
    val likedFilter = remember { mutableStateOf(false) }

    Row(modifier = Modifier.padding(start = 20.dp)) {
        Text(
            text = "Filters :",
            modifier = Modifier.align(CenterVertically).padding(end = 50.dp)
        )


        Text(
            text = "Friends",
            modifier = Modifier.align(CenterVertically)
        )
        Checkbox(
            checked = friendsFilter.value,
            enabled = true,
            onCheckedChange = {
                friendsFilter.value = it
                checkedBoxes["Friends"] = it
                updateFilteredTrips(checkedBoxes.toMap())
            }
        )

        Text(
            text = "Liked",
            modifier = Modifier.align(CenterVertically)
        )
        Checkbox(
            checked = likedFilter.value,
            enabled = true,
            onCheckedChange = {
                likedFilter.value = it
                checkedBoxes["Liked"] = it
                updateFilteredTrips(checkedBoxes.toMap())
            }
        )
    }
}