package components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.GsonBuilder
import commonGreen
import data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.codebot.models.*
import org.jetbrains.skia.Image
import uploadImage
import validateTrip
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*


@Composable
fun YelpLocationInput(
    onChange: (List<String>) -> Unit,
) {
    val searchLocationInput = remember { mutableStateOf("") }
    val searchTermInput = remember { mutableStateOf("") }
    val displayBusinesses = remember { mutableStateOf(false) }

    Box {
        Column {
            Row {
                OutlinedTextField(
                    value = searchLocationInput.value,
                    onValueChange = { newText ->
                        searchLocationInput.value = newText
                        displayBusinesses.value = false
                    },
                    label = { Text("Location") }
                )
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedTextField(
                    value = searchTermInput.value,
                    onValueChange = { newText ->
                        searchTermInput.value = newText
                        displayBusinesses.value = false
                    },
                    label = { Text("What are you looking for?") }
                )
                TextButton(
                    onClick = { displayBusinesses.value = true },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                { Text("Go!") }
            }
            if (displayBusinesses.value) {
                val client = HttpClient.newBuilder().build()
                // NOTE: the following is a PRIVATE yelp api key. As long as the project remains internal and private,
                //    having it here is fine, but if it were to be made public/others wanted to run it, they would need
                //    their own yelp api key and there would need to be some implementation of secrets.
                val yelpApiKey =
                    "DNRN37NKZ1EfitDfgRMMCIFJZhkYWKsz8E7NyYhcUVzGLORHS9m1npxNuT70yWuMP_eCNJgSn2GMtd0vqSOjOZirKO43Qt6jVY4ak5spFRRVWoAogh1_TJ0Aa4lEZXYx"

                val request = HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "https://api.yelp.com/v3/businesses/search?location=${
                                searchLocationInput.value.lowercase().replace(' ', '-')
                            }&term=${
                                searchTermInput.value.lowercase().replace(' ', '-')
                            }&categories=&sort_by=best_match&limit=5"
                        )
                    )
                    .header("accept", "application/json")
                    .header("Authorization", "Bearer $yelpApiKey")
                    .GET()
                    .build()

                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                val responseBody = response.body()
                val gson = GsonBuilder().create()
                val queryInfo = gson.fromJson(responseBody, YelpQueryInfo::class.java)
                print(queryInfo.businesses)
                if (queryInfo.businesses.isEmpty()) {
                    Text("No results for query.")
                }
                DropdownMenu(
                    expanded = displayBusinesses.value,
                    onDismissRequest = { displayBusinesses.value = false }) {
                    queryInfo.businesses.forEach { business ->
                        DropdownMenuItem(onClick = {
                            onChange(listOf(business.name, business.location.city))
                            displayBusinesses.value = false
                        }) {
                            Text(business.name + ", " + business.location.city)
                        }
                    }
                }
            }
        }
    }
}

// dropdown code is from https://stackoverflow.com/questions/67111020/exposed-drop-down-menu-for-jetpack-compose
@Composable
fun AddTripPage(mode: String, trip: Trip? = null, userId: Int, onSubmit: (Trip?) -> Unit) {


    val name = remember { mutableStateOf(trip?.name ?: "") }
    val description = remember { mutableStateOf(trip?.description ?: "") }
    val startTime = remember { mutableStateOf(trip?.startTime ?: "") }
    val endTime = remember { mutableStateOf(trip?.endTime ?: "") }
    val image = remember { mutableStateOf(trip?.image ?: "") }
    val status = remember { mutableStateOf(trip?.status ?: "") }
    val pins = remember { mutableStateListOf(Pin(0, 0, 0, "", "", "", "")) }

    val errorMessages = remember { mutableStateOf(mutableListOf<String>()) }
    val openDialog = remember { mutableStateOf(false) }

    if (trip != null && pins.size == 1 && pins[0].city == "") {
        pins.removeAt(0)
        pins.addAll(pinsOf(trip.id))
        if (pins.size == 0) {
            pins.add(Pin(0, 0, 0, "", "", "", ""))
        }
    }
    val expanded = remember { mutableStateOf(false) }
    val dropDownIcon = if (expanded.value) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    if (openDialog.value) {
        ErrorDialog(errorMessages.value, openDialog)
    }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        item {
            Text(
                if (mode == "Create") "Add New Trip" else "Edit existing trip",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            OutlinedTextField(value = name.value, onValueChange = { name.value = it }, label = { Text("Name") })
        }

        item {
            OutlinedTextField(
                value = description.value,
                onValueChange = { description.value = it },
                label = { Text("Description") })
        }

        item {
            OutlinedTextField(
                value = startTime.value,
                onValueChange = { startTime.value = it },
                label = { Text("Start Date (DD/MM/YYYY)") })
        }

        item {
            OutlinedTextField(
                value = endTime.value,
                onValueChange = { endTime.value = it },
                label = { Text("End Date (DD/MM/YYYY)") })
        }

        item {
            ImageInput(image.value, { image.value = it })
        }

        item {
            Box {
                OutlinedTextField(
                    value = status.value,
                    onValueChange = { status.value = it },
                    label = { Text("Status") },
                    trailingIcon = {
                        Icon(dropDownIcon, "contentDescription",
                            Modifier.clickable { expanded.value = !expanded.value })
                    }
                )

                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                ) {
                    DropdownMenuItem(onClick = {
                        status.value = "Planned"
                        expanded.value = false
                    }) {
                        Text("Planned")
                    }
                    DropdownMenuItem(onClick = {
                        status.value = "In Progress"
                        expanded.value = false
                    }) {
                        Text("In Progress")
                    }
                    DropdownMenuItem(onClick = {
                        status.value = "Completed"
                        expanded.value = false
                    }) {
                        Text("Completed")
                    }
                }
            }
        }

        item {
            Text("Add Stops:", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column {
                Text("Use this Yelp Input to add a stop to your trip!")
                Spacer(modifier = Modifier.width(5.dp))
                YelpLocationInput(onChange = {
                    if (pins.size == 1 && pins[0].city == "" && pins[0].location == "" && pins[0].description == "") {
                        pins[0] = (Pin(0, 0, 0, it[1], it[0], "", ""))
                    } else {
                        pins.add(Pin(0, 0, 0, it[1], it[0], "", ""))
                    }
                })
            }
        }

        itemsIndexed(pins) { i, pin ->
            PinInput(
                i,
                pins,
                onDelete = {
                    pins.removeAt(i)
                    print("delete $i")
                },
                onMoveUp = {
                    swap(pins, i, i - 1)
                },
                onMoveDown = {
                    swap(pins, i, i + 1)
                },
            )
        }

        item {
            Row {
                Spacer(modifier = Modifier.width(100.dp))
                IconButton(onClick = {
                    pins.add(Pin(0, 0, 0, "", "", "", ""))
                }) {
                    Icon(Icons.Rounded.Add, contentDescription = "add")
                }
            }
        }

        item {
            Row(modifier = Modifier.padding(start = 25.dp)) {
                Button(
                    onClick = {
                        val messages = validateTrip(
                            name.value,
                            description.value,
                            startTime.value,
                            endTime.value,
                            status.value,
                            pins
                        )
                        if (messages.size == 0
                        ) {
                            if (mode == "Create") {
                                val toInsert = TripInsert(
                                    userId,
                                    name.value,
                                    description.value,
                                    startTime.value,
                                    endTime.value,
                                    0,
                                    image.value,
                                    status.value
                                )
                                val tripId = addTrip(toInsert).toInt()
                                pins.forEach { pin ->
                                    val pinInsert =
                                        PinInsert(userId, tripId, pin.city, pin.location, pin.description, pin.image)
                                    addPin(pinInsert)
                                }

                            } else if (mode == "Edit") {
                                val toInsert = TripUpdate(
                                    userId = userId,
                                    name = name.value,
                                    description = description.value,
                                    startTime = startTime.value,
                                    endTime = endTime.value,
                                    score = trip?.score,
                                    image = image.value,
                                    status = status.value
                                )
                                editTrip(toInsert, trip?.id)
                                if (trip != null) {
                                    deletePins(trip.id)
                                    pins.forEach { pin ->
                                        val pinInsert =
                                            PinInsert(
                                                userId,
                                                trip.id,
                                                pin.city,
                                                pin.location,
                                                pin.description,
                                                pin.image
                                            )
                                        addPin(pinInsert)

                                    }
                                }
                            }
                            CoroutineScope(Dispatchers.Default).launch {
                                onSubmit(
                                    trip?.copy(
                                        trip.id,
                                        userId,
                                        name.value,
                                        description.value,
                                        startTime.value,
                                        endTime.value,
                                        0,
                                        image.value,
                                        status.value
                                    )
                                )
                            }

                        } else {
                            //print(messages)
                            errorMessages.value.addAll(messages)
                            openDialog.value = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = commonGreen, contentColor = Color.White),
                ) {
                    Text("Submit")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { onSubmit(null) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = commonGreen, contentColor = Color.White),
                ) {
                    Text("Cancel")
                }
            }
        }

    }
}

@Composable
fun ImageInput(ogImage: String, onChange: (String) -> Unit) {
    val image = remember { mutableStateOf("") }
    val imageUpload = remember { mutableStateOf("") }

    Text("Image Upload:", fontSize = 17.sp, fontWeight = FontWeight.Bold)
    TextButton(onClick = {
        imageUpload.value = uploadImage(ComposeWindow(), "Select Image(png or jpeg) to Upload.")
    }) {
        Text("Select File")
    }
    Row {
        Column {
            Text("Selected File:")
            Spacer(modifier = Modifier.width(5.dp))
            if (imageUpload.value == "") {
                if (listOf("img", "image", "").contains(ogImage)) {
                    Text("No File Uploaded.")
                } else {
                    var imageAsset: ImageBitmap? = null
                    try {
                        val imageBytes = Base64.getDecoder().decode(ogImage)
                        imageAsset = Image.makeFromEncoded(imageBytes).toComposeImageBitmap()

                    } catch (e: Exception) {
                        print(e.toString())
                    }
                    if (imageAsset != null) {
                        Image(imageAsset, "display pin image", modifier = Modifier.width(200.dp))
                    }
                }
            } else {
                var imageAsset: ImageBitmap? = null
                try {
                    val imageBytes = Base64.getDecoder().decode(imageUpload.value)
                    imageAsset = Image.makeFromEncoded(imageBytes).toComposeImageBitmap()

                } catch (e: Exception) {
                    print(e.toString())
                }
                if (imageAsset != null) {
                    Image(imageAsset, "display image upload", modifier = Modifier.width(200.dp))
                    image.value = imageUpload.value
                    onChange(imageUpload.value)
                }
            }
        }
    }
}

fun swap(pins: MutableList<Pin>, initial: Int, target: Int) {
    val temp = pins[initial]
    pins[initial] = pins[target]
    pins[target] = temp
}

@Composable
fun ErrorDialog(messages: MutableList<String>, openDialog: MutableState<Boolean>) {
    AlertDialog(
        onDismissRequest = {
            messages.clear()
            openDialog.value = false
        },
        buttons = {
            TextButton(onClick = {
                messages.clear()
                openDialog.value = false
            }) {
                Text("Dismiss")
            }
        },
        title = {
            Text("You have some errors :(")
        },
        text = {
            LazyColumn {
                item {
                    Text("Please fix the following errors:")
                }
                items(messages) {
                    Text(it)
                }
            }
        }
    )
}