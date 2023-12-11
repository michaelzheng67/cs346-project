package components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import data.removeFriends
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.codebot.models.FriendInsert
import net.codebot.models.User

@Composable
fun RemoveFriend(operatingUserId: Int, friend: User, friends: MutableState<List<User>>) {
    TextButton(
        onClick = {
            CoroutineScope(Dispatchers.Default).launch {
                removeFriends(FriendInsert(operatingUserId, friend.id))
                friends.value -= friend
            }
        },
        modifier = Modifier.wrapContentHeight().fillMaxWidth()
    )
    { Text("Remove? ", color = Color.White, fontSize = 10.sp) }
}