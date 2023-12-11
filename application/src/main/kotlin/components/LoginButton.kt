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
fun LoginButton(authManager: AuthManager, setUserId: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to GOOGLE.ai!", fontSize = 60.sp)
        Text("Login using the button below.", fontSize = 60.sp)
        Spacer(modifier = Modifier.size(30.dp))
        Button(
            onClick = {
                authManager.authenticateUser(
                    // domain and clientId should be obscured in some manner
                    //  if code is ever made public
                    domain = "dev-0bhppsrjg2wl8bps.us.auth0.com",
                    clientId = "OY4iCnotqMxSxowCBTZjq7IfAV9t2hWl",
                    redirectUri = "http://localhost:5789/callback",
                    scope = "openid offline_access profile",
                    audience = "api1",
                )
                setUserId(authManager.getAuthId())

            },
            colors = ButtonDefaults.buttonColors(backgroundColor = commonGreen, contentColor = Color.White),
            modifier = Modifier.height(250.dp).width(450.dp)
        ) {
            Text("Login", fontSize = 90.sp)
        }
    }


}