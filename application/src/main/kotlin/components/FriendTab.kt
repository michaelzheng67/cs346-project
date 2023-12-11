package components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.codebot.models.User
import org.jetbrains.skia.Image
import java.util.*

@Composable
fun FriendTab(friend: User, onclick: () -> Unit) {
    TextButton(onClick = onclick, modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 50.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfilePicture(friend.image, false, 40.dp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(friend.name, color = Color.White)
        }
    }
}

@Composable
fun ProfilePicture(image: String, isHeight: Boolean, dimension: Dp) {
    //When no image is provided, fill with placeholder image
    if (listOf("img", "image", "").contains(image)) {
        Image(
            painter = painterResource("placeholderPFP.png"),
            contentDescription = "display pfp",
            modifier = if (isHeight) Modifier.height(dimension) else Modifier.width(dimension)
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
            Image(
                imageAsset,
                "display pfp",
                modifier = if (isHeight) Modifier.height(dimension) else Modifier.width(dimension)
            )
        }

    }
}