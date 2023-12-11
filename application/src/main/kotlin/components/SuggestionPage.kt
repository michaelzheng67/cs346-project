package components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import commonGreen
import data.getGptSuggestions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Preview
@Composable
fun SuggestionPage(userid: Int) {
    val displayText =
        remember { mutableStateOf("Hi! Press the button below to get some suggestions for your next trip! After pressing the button, please give Trip some time to think - he's trying his best.") }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Need inspiration for the next trip? Why not ask Trip Tripowski for some advice!")
        Row {
            Image(painter = painterResource("mascot.png"), "Mascot", modifier = Modifier.width(400.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.height(275.dp).width(400.dp)) {
                Image(
                    painter = painterResource("speechbubble.png"),
                    "Speech Bubble",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.height(275.dp).width(400.dp)
                )
                Row {
                    Spacer(modifier = Modifier.width(30.dp))
                    Column {
                        Text(
                            displayText.value,
                            modifier = Modifier.width(250.dp)
                        )
                        Spacer(modifier = Modifier.height(15.dp))
                    }
                }
            }
        }
        Button(
            onClick = {
                displayText.value = "Hmm, let me think..."
                CoroutineScope(Dispatchers.Default).launch {
                    displayText.value = getGptSuggestions(userid)
                }
            }, colors = ButtonDefaults.buttonColors(backgroundColor = commonGreen, contentColor = Color.White),
            modifier = Modifier.padding(start = 25.dp)
        ) {
            Text("Get Suggestions")
        }
    }
}