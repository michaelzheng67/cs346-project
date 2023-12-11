package components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import commonGreen
import data.getUserById
import data.insertFriends
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.codebot.models.FriendInsert
import net.codebot.models.User
import java.io.FileNotFoundException


@Composable
fun AddFriend(operatingUser: User, friends: MutableState<List<User>>) {
    val textInput = remember { mutableStateOf("") }
    val showUserNotExists = remember { mutableStateOf(false) }
    val showCantSelfFriend = remember { mutableStateOf(false) }
    Column {
        TextField(
            value = textInput.value,
            onValueChange = { textInput.value = it },
            label = { Text("Add Friend: (name#id)") },
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
            modifier = Modifier.border(BorderStroke(2.dp, commonGreen))
        )

        TextButton(
            onClick = {
                CoroutineScope(Dispatchers.Default).launch {
                    //This regex requires the input to be of format "...#..."
                    //Where "..." is a non-empty string
                    if(!Regex(".+#.+").containsMatchIn(textInput.value)) {
                        showUserNotExists.value = true
                        delay(3000)
                        showUserNotExists.value = false
                    }

                    val id = textInput.value.substringAfterLast("#")
                    val name = textInput.value.substringBeforeLast("#")
                    if (name == operatingUser.name && id.toInt() == operatingUser.id) {
                        showCantSelfFriend.value = true
                        delay(3000)
                        showCantSelfFriend.value = false
                    } else {
                        val userById: User
                        try {
                            userById = getUserById(id.toInt())

                            if (userById.name == name) {
                                insertFriends(FriendInsert(operatingUser.id, id.toInt()))
                                friends.value += userById
                            } else {
                                showUserNotExists.value = true
                                delay(3000)
                                showUserNotExists.value = false
                            }
                        } catch (e: FileNotFoundException) { //For some reason this corresponds to not found
                            showUserNotExists.value = true
                            delay(3000)
                            showUserNotExists.value = false
                        }
                    }

                }

            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        { Text("Add This Friend!", color = Color.White) }
        if (showUserNotExists.value) {
            AlertDialog(
                { showUserNotExists.value = false },
                {
                    TextButton({ showUserNotExists.value = false }) {
                        Text("Dismiss")
                    }
                },
                text = { Text("User added doesn't exist, check formatting") })
        }
        if (showCantSelfFriend.value) {
            AlertDialog(
                { showCantSelfFriend.value = false },
                {
                    TextButton({ showCantSelfFriend.value = false }) {
                        Text("Dismiss")
                    }
                },
                text = { Text("You can't add yourself as a friend") })
        }
    }

}