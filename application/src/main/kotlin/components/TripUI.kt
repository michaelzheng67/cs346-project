package components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import commonGreen
import data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.codebot.models.FriendInsert
import net.codebot.models.Pin
import net.codebot.models.Trip
import net.codebot.models.User
import org.jetbrains.skia.Image
import java.util.*

@Composable
fun TripSelectedView(
    selectedTrip: MutableState<Trip>, selectedTripPins: MutableState<MutableList<Pin>>, operatingUser: Int,
    selectedTab: MutableState<String>,
    tripToEdit: MutableState<Trip>,
    friends: MutableState<List<User>>,
    onDelete: (Int) -> Unit
) {
    val viewDialog = remember { mutableStateOf(false) }
    if (viewDialog.value) {
        DeleteDialog(
            selectedTrip.value.id,
            selectedTrip.value.name,
            {
                deleteTrip(selectedTrip.value.id)
                onDelete(selectedTrip.value.id)
                viewDialog.value = false
            },
            { viewDialog.value = false })
    }
    LazyColumn(modifier = Modifier.fillMaxWidth().padding(3.dp)) {
        item {
            TripThumbnail(selectedTrip.value.image, 400.dp, 300.dp)
            Text(selectedTrip.value.name, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(selectedTrip.value.description)
            Text(selectedTrip.value.startTime + " - " + selectedTrip.value.endTime)
            Text("Status: " + selectedTrip.value.status)
            Text("Score: " + selectedTrip.value.score.toString())
            Row {

                if (selectedTrip.value.userId == operatingUser) {
                    Row {
                        IconButton({
                            tripToEdit.value = selectedTrip.value
                            selectedTab.value = "EditTrip"
                        }) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = "Edit Icon",
                                tint = Color.Black,
                            )
                        }
                        IconButton({
                            viewDialog.value = true
                        }) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Delete Icon",
                                tint = Color.Black,
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.Default).launch {
                                //friends.value = false
                                insertFriends(FriendInsert(operatingUser, selectedTrip.value.userId))
                                friends.value += getUserById(selectedTrip.value.userId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = commonGreen, contentColor = Color.White),
                    ) { Text("Add author as Friend") }
                }
            }

            Text("Stops:", fontSize = 17.sp)
        }

        items(selectedTripPins.value) { pin ->
            val expanded = mutableStateOf(false)
            PinView(pin, expanded)
        }
    }
}

@Composable
fun TripButton(
    trip: Trip,
    onclick: () -> Unit,
    operatingUser: Int,
    selectedTab: MutableState<String>,
    tripToEdit: MutableState<Trip>,
    liked: MutableState<Boolean>,
    score: MutableState<Int>,
    onLike: () -> Unit,
    onDislike: () -> Unit,
) {

    OutlinedButton(onClick = onclick) {
        Column {
            TripThumbnail(trip.image, 400.dp, 300.dp)
            Text(trip.name, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {

                    if (liked.value) {
                        CoroutineScope(Dispatchers.Default).launch {
                            unlikeTrip(operatingUser, trip.id)
                        }
                        score.value -= 1
                        onDislike()
                    } else {
                        CoroutineScope(Dispatchers.Default).launch {
                            likeTrip(operatingUser, trip.id)
                        }
                        score.value += 1
                        onLike()
                    }
                    liked.value = !liked.value


                }) {
                    Icon(
                        if (liked.value) Icons.Rounded.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Like Icon",
                        tint = if (liked.value) commonGreen else Color.Black,
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
                Text(score.value.toString())
                if (trip.userId == operatingUser) {
                    IconButton({
                        tripToEdit.value = trip
                        selectedTab.value = "EditTrip"
                    }) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = "Edit Icon",
                            tint = Color.Black,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TripThumbnail(image: String, width: Dp, height: Dp) {
    if (listOf("img", "image", "").contains(image)) {
        Image(
            painter = painterResource("tripplaceholder.jpg"),
            contentDescription = null,
            modifier = Modifier.width(width)
        )
    } else {
        var imageAsset: ImageBitmap? = null
        try {
            val imageBytes = Base64.getDecoder().decode(image)
            imageAsset = Image.makeFromEncoded(imageBytes).toComposeImageBitmap()

        } catch (e: Exception) {
            print(e.toString())
        }
        if (imageAsset != null) {
            Image(imageAsset, "display trip image", modifier = Modifier.width(width))
        }

    }
}

@Composable
fun DeleteDialog(id: Int, name: String, onDelete: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        buttons = {
            Row {
                TextButton(onClick = { onDelete(id) }) {
                    Text("Delete")
                }
                Spacer(modifier = Modifier.width(10.dp))
                TextButton(onClick = { onDismiss() }) {
                    Text("Cancel")
                }
            }
        },
        title = {
            Text("Are you sure you want to delete trip $name ?")
        },
    )
}