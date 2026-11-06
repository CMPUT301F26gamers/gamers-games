package com.example.campsiteapp

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun WelcomeScreen (
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repositiory = ProfileRepository()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Green header
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xFF006838))
        )

        Text(
            text = "CampLotto",
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier.padding(top = 24.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Welcome back! This device is\nrecognized, no username or\npassword needed.",
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                val deviceId = getDeviceId(context)
                repositiory.createOrLoadProfile(deviceId, { profile ->
                    AppUser.profile = profile
                })
                navController.navigate("dashboard")
                      },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF006838)
            ),
            modifier = Modifier
                .width(200.dp)
                .height(50.dp)
        ) {
            Text("Start")
        }

        Spacer(modifier = Modifier.weight(1f))

        // Green footer
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xFF006838))
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(navController = NavController(LocalContext.current))
}

fun getDeviceId(context: Context): String {
    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
}