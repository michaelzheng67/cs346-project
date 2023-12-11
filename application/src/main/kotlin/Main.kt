import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.google.gson.Gson
import components.*
import data.*
import net.codebot.models.Trip
import net.codebot.models.User
import net.codebot.models.WindowDetails
import java.awt.Dimension
import java.io.File

fun main() = application {

    // get the window states
    val windowDetails = loadWindowSize()
    val windowState = WindowState(
        width = windowDetails.width.dp,
        height = windowDetails.height.dp,
        position = WindowPosition(windowDetails.x.dp, windowDetails.y.dp)
    )

    Window(
        onCloseRequest = {

            // saves window first before closing
            print(windowState.position.x.toString() + " " + windowState.position.y.toString())
            saveWindowSize(
                windowState.size.width.value.toInt(),
                windowState.size.height.value.toInt(),
                windowState.position.x.value.toInt(),
                windowState.position.y.value.toInt()
            )
            exitApplication()
        },
        state = windowState
    ) {
        window.minimumSize = Dimension(1200, 800)
        MainWindow()
    }
}

// load the window to either default or saved size on startup
fun loadWindowSize(): WindowDetails {
    val windowSizeFilePath = System.getProperty("user.home") + File.separator + "window_size.json"
    val windowSizeFile = File(windowSizeFilePath)

    if (windowSizeFile.exists()) {
        val json = windowSizeFile.readText()
        return Gson().fromJson(json, WindowDetails::class.java)
    }
    return WindowDetails(900, 600, 100, 100) // Default parameters
}


// save the window size on close
fun saveWindowSize(width: Int, height: Int, x: Int, y: Int) {
    val windowSizeFilePath = System.getProperty("user.home") + File.separator + "window_size.json"
    val windowSizeFile = File(windowSizeFilePath)

    val windowSize = WindowDetails(width, height, x, y)
    val json = Gson().toJson(windowSize)
    windowSizeFile.writeText(json)
}

@Composable
@Preview
fun MainWindow() {
    val loggedIn = remember { mutableStateOf(false) }
    var authManager = remember { AuthManager() }
    val operatingUser = remember { mutableStateOf(User(0, "", "", "", "", "", "", "", "")) }
    val operatingUserId = remember { mutableStateOf(-1) }

    val selectedTab = remember { mutableStateOf("Profile") }
    val selectedUser = remember { mutableStateOf(operatingUser.value) }
    val viewTrip = remember { mutableStateOf(false) }

    val friendList = mutableStateOf(friendsOf(operatingUserId.value))
    val tripToEdit = remember { mutableStateOf(Trip(0, 0, "", "", "", "", 0, "", "")) }


    val allTrips = remember { mutableStateOf(allTrips().toMutableList()) }
    var allTripsIDs = allTrips.value.map { it.id }


    val likedTripsIDs = mutableStateOf(likedTrips(operatingUserId.value).map { it.id }.toMutableList())
    val friendsIDs = friendList.value.map { it.id }

    operatingUser.value = operatingUser.value.copy(avatar = userAvatar(operatingUserId.value))

    if (loggedIn.value) {
        MaterialTheme {
            Column(modifier = Modifier.onPreviewKeyEvent {
                when {
                    (it.isCtrlPressed && it.key == Key.N) -> {
                        selectedTab.value = "NewTrip"
                        true
                    }

                    (it.isCtrlPressed && it.key == Key.P) -> {
                        selectedTab.value = "Profile"
                        selectedUser.value = operatingUser.value
                        true
                    }

                    (it.isCtrlPressed && it.key == Key.E) -> {
                        selectedTab.value = "Explore"
                        true
                    }

                    (it.isCtrlPressed && it.key == Key.U) -> {
                        selectedTab.value = "EditUser"
                        true
                    }

                    (it.isCtrlPressed && it.key == Key.T) -> {
                        selectedTab.value = "Suggestions"
                        true
                    }


                    else -> false
                }
            }) {
                Navbar(selectedTab, profileOnClick = { selectedUser.value = operatingUser.value })

                Row {
                    FriendsBar(selectedUser, selectedTab, operatingUser.value, { viewTrip.value = false }, friendList)

                    when (selectedTab.value) {
                        "Profile" -> {
                            viewTrip.value = false
                            ProfilePage(
                                selectedUser.value,
                                allTrips,
                                selectedTab,
                                tripToEdit,
                                operatingUserId.value,
                                likedTripsIDs,
                                friendList,
                                viewTrip
                            )
                        }

                        "Explore" -> {
                            if (!askNewTrips(allTripsIDs)) {
                                //reload data
                                allTrips.value = allTrips().toMutableList()
                                allTripsIDs = allTrips.value.map { it.id }
                            }

                            ExplorePage(
                                operatingUserId.value,
                                allTrips,
                                likedTripsIDs,
                                friendsIDs,
                                selectedTab,
                                tripToEdit,
                                friendList
                            )
                        }

                        "NewTrip" -> {
                            AddTripPage("Create", userId = operatingUserId.value, onSubmit = {
                                selectedTab.value = "Profile"
                                selectedUser.value = operatingUser.value
                            })
                        }

                        "EditTrip" -> {
                            AddTripPage("Edit", trip = tripToEdit.value, userId = operatingUserId.value, onSubmit = {
                                val index = allTrips.value.indexOfFirst { it.id == tripToEdit.value.id }
                                if (it != null) {
                                    allTrips.value[index] = it
                                }
                                selectedTab.value = "Profile"
                                selectedUser.value = operatingUser.value
                            })
                        }

                        "EditUser" -> {
                            EditUserPage(operatingUser.value, { operatingUser.value = it }, {
                                selectedTab.value = "Profile"
                                selectedUser.value = operatingUser.value
                            })
                        }

                        "Suggestions" -> {
                            SuggestionPage(operatingUserId.value)
                        }
                    }
                }
            }
        }
    } else {
        var isLoggingIn = authManager.isLoggingIn.collectAsState(false)
        var userIdState = authManager.userId.collectAsState()
        var authId: String

        Column {
            if (!(isLoggingIn.value)) {
                LoginButton(authManager, setUserId = { authId = it })
            } else {
                if (userIdState.value != "") {
                    LoginLoading()
                    val user_exists = userExists(authManager.getAuthId())
                    println(user_exists)
                    if (!user_exists) {
                        addUser(authManager.getAuthId(), name = "Change your name in the Edit User page")
                        println("Done")
                    }
                    authId =
                        userIdState.value
                    println("authid: $authId")

                    operatingUserId.value = userIdFromAuthId(authId)
                    loggedIn.value = true
                    operatingUser.value = getUserById(operatingUserId.value)
                    selectedUser.value = operatingUser.value
                } else {
                    CancelLoginButton(authManager)
                    // reset auth manager
                    authManager = AuthManager()
                    isLoggingIn = authManager.isLoggingIn.collectAsState(false)
                    userIdState = authManager.userId.collectAsState()
                }
            }
        }
    }


}




















