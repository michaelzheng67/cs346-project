package components

import AuthManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import commonGreen

@Composable
fun CancelLoginButton(authManager: AuthManager) {
    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "You can cancel login using the button below.", fontSize = 40.sp
        )
        Spacer(modifier = Modifier.size(30.dp))
        Button(
            onClick = {
                authManager.cancelLogin()
            }, colors = ButtonDefaults.buttonColors(backgroundColor = commonGreen, contentColor = Color.White),
            modifier = Modifier.height(250.dp).width(450.dp)
        ) {
            Text("Cancel", fontSize = 90.sp)
        }
        Spacer(modifier = Modifier.size(30.dp))
        Text(
            "Once you sign in on your browser, please allow some time for the login process to complete.",
            fontSize = 25.sp
        )
    }
}