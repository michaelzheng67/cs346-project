package components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import net.codebot.models.Pin
import org.jetbrains.skia.Image
import java.util.*

@Composable
fun PinView(pin: Pin, expanded: MutableState<Boolean>) {
    val dropDownIcon = if (expanded.value) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    Column {
        Row {
            Icon(
                Icons.Rounded.Place,
                contentDescription = "Pin Icon",
                tint = Color.Black,
                modifier = Modifier.align(alignment = Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(pin.location + ", " + pin.city, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                dropDownIcon,
                contentDescription = "Pin Dropdown",
                Modifier.clickable { expanded.value = !expanded.value }.align(alignment = Alignment.CenterVertically)
            )

        }
        if (expanded.value) {
            Spacer(modifier = Modifier.height(5.dp))
            PinThumbnail(pin.image, 320.dp)
            Text(pin.description)
        }
    }
}

@Composable
fun PinInput(
    i: Int,
    pins: SnapshotStateList<Pin>,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Row {
        Column {
            Text("Stop " + (i + 1).toString(), fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Row {
                OutlinedTextField(pins[i].location, onValueChange = {
                    pins[i] = pins[i].copy(location = it)
                }, label = { Text("Location") }, modifier = Modifier.align(alignment = Alignment.CenterVertically))

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedTextField(pins[i].city, onValueChange = {
                    pins[i] = pins[i].copy(city = it)
                }, label = { Text("City") }, modifier = Modifier.align(alignment = Alignment.CenterVertically))

                IconButton(onClick = {
                    if (pins.size > 1) {
                        onDelete()
                    }
                }, modifier = Modifier.align(alignment = Alignment.CenterVertically)) {
                    Icon(Icons.Rounded.Delete, contentDescription = "delete")
                }
                Column(modifier = Modifier.align(alignment = Alignment.CenterVertically)) {
                    if (i != 0) {
                        IconButton(onClick = {
                            onMoveUp()
                        }, modifier = Modifier.wrapContentSize()) {
                            Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "move up")
                        }
                    }
                    if (i != pins.size - 1) {
                        IconButton(onClick = {
                            onMoveDown()
                        }, modifier = Modifier.wrapContentSize()) {
                            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "move down")
                        }
                    }
                }

            }

            OutlinedTextField(pins[i].description, onValueChange = {
                pins[i] = pins[i].copy(description = it)
            }, label = { Text("Description") })

            Spacer(modifier = Modifier.height(5.dp))

            ImageInput(pins[i].image, {
                pins[i] = pins[i].copy(image = it)
            })
        }

    }
}

@Composable
fun PinThumbnail(image: String, width: Dp) {
    if (listOf("img", "image", "").contains(image)) {
        Image(
            painter = painterResource("pinplaceholder.jpg"),
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
            Image(imageAsset, "display pin image", modifier = Modifier.width(width))
        }

    }
}