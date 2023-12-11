package components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import commonGreen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Navbar(selectedTab: MutableState<String>, profileOnClick: () -> Unit) {


    Row(modifier = Modifier.height(75.dp).background(color = commonGreen).fillMaxWidth()) {
        TooltipArea(tooltip = {
            Text(
                "Going On Outstanding Global Leisure Excursions ! Amazing Landscapes",
                color = Color.White,
                modifier = Modifier.background(Color.Gray).align(Alignment.CenterVertically)
            )
        }) {
            Text(
                "GOOGLE.ai",
                fontSize = 30.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(15.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = {
                selectedTab.value = "Profile"
                profileOnClick()
            },
            modifier = Modifier.fillMaxHeight().width(80.dp)
        ) {
            Text("Profile", color = Color.White)
        }
        Spacer(modifier = Modifier.width(20.dp))
        TextButton(
            onClick = { selectedTab.value = "Explore" },
            modifier = Modifier.fillMaxHeight().width(80.dp)
        ) {
            Text("Explore", color = Color.White)
        }
        Spacer(modifier = Modifier.width(20.dp))
        TextButton(
            onClick = { selectedTab.value = "NewTrip" },
            modifier = Modifier.fillMaxHeight().wrapContentWidth()
        ) {
            Text("New Trip", color = Color.White)
        }
        Spacer(modifier = Modifier.width(20.dp))
        TextButton(
            onClick = { selectedTab.value = "EditUser" },
            modifier = Modifier.fillMaxHeight().wrapContentWidth()
        ) {
            Text("Edit User Profile", color = Color.White)
        }
        Spacer(modifier = Modifier.width(20.dp))
        TextButton(
            onClick = { selectedTab.value = "Suggestions" },
            modifier = Modifier.fillMaxHeight().wrapContentWidth()
        ) {
            Text("Trip Suggestions", color = Color.White)
        }
        Spacer(modifier = Modifier.width(20.dp))
    }
}