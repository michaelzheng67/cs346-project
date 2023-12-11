package components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import commonGreen
import net.codebot.models.User

@Composable
fun FriendsBar(
    selectedUser: MutableState<User>,
    selectedTab: MutableState<String>,
    operatingUser: User,
    changeView: () -> Unit,
    friends: MutableState<List<User>>
) {
    Column(modifier = Modifier.width(150.dp).background(color = commonGreen).fillMaxHeight()) {
        Text(
            "Logged In As:",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        )
        FriendTab(operatingUser, onclick = {
            selectedUser.value = operatingUser
            changeView()
            selectedTab.value = "Profile"
        })
        Text(
            "Your Friends",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(friends.value) { friend ->
                FriendTab(friend, onclick = {
                    selectedUser.value = friend
                    changeView()
                    selectedTab.value = "Profile"
                })
                RemoveFriend(operatingUser.id, friend, friends)
            }
        }
        Spacer(modifier = Modifier.width(5.dp))
        AddFriend(operatingUser, friends)
    }
}