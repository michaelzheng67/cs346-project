package components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import commonGreen
import data.editUser
import net.codebot.models.User
import net.codebot.models.UserUpdate

@Composable
fun EditUserPage(user: User, setOperatingUser: (User) -> Unit, onSubmit: () -> Unit) {
    val name = remember { mutableStateOf(user.name) }
    val bio = remember { mutableStateOf(user.bio) }
    val image = remember { mutableStateOf(user.image) }

    val errorMessages = remember { mutableStateOf(mutableListOf<String>()) }
    val openDialog = remember { mutableStateOf(false) }

    if (openDialog.value) {
        ErrorDialog(errorMessages.value, openDialog)
    }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        item {
            Text(
                "Edit User Profile:",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        item {
            OutlinedTextField(value = name.value, onValueChange = { name.value = it }, label = { Text("Name") })
        }
        item {
            OutlinedTextField(value = bio.value, onValueChange = { bio.value = it }, label = { Text("Bio") })
        }
        item {
            ImageInput(image.value, { image.value = it })
        }

        item {
            Row(modifier = Modifier.padding(start = 25.dp)) {
                Button(
                    onClick = {
                        if (name.value != "" && bio.value != "") {
                            val toUpdate = UserUpdate(
                                user.email,
                                user.password,
                                name.value,
                                bio.value,
                                user.joined,
                                image.value,
                                user.auth_id,
                                user.avatar
                            )
                            editUser(toUpdate, user.id)
                            setOperatingUser(user.copy(name = name.value, bio = bio.value, image = image.value))
                            onSubmit()
                        } else {
                            if (name.value == "") {
                                errorMessages.value.add("Invalid name.")
                            }
                            if (bio.value == "") {
                                errorMessages.value.add("Invalid bio.")
                            }
                            openDialog.value = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = commonGreen, contentColor = Color.White),
                ) {
                    Text("Submit")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { onSubmit() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = commonGreen, contentColor = Color.White),
                ) {
                    Text("Cancel")
                }
            }
        }

    }
}