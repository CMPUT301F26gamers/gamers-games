package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController

@Composable
fun DashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val profile = AppUser.profile

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
            text = "Dashboard",
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier.padding(20.dp)
        )

        // Common features
        DashboardButton("Campsites", navController, "")
        DashboardButton("My Profile", navController, "profile")

        // Entrant features
        if (profile?.hasRole(Roles.ENTRANT) == true) {
            // Add entrant-specific buttons here
        }

        // Owner features
        if (profile?.hasRole(Roles.OWNER) == true) {
            // Add owner-specific buttons here
        }

        // Admin features
        if (profile?.hasRole(Roles.ADMIN) == true) {
            DashboardButton("Profiles", navController, "browseProfiles")
            DashboardButton("Images",  navController, "")
            DashboardButton("Notification Logs", navController, "")
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

@Composable
fun DashboardButton(
    text: String,
    navController: NavController,
    nextPage: String
) {
    Button(
        onClick = {navController.navigate(nextPage)} ,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.LightGray,
            contentColor = Color.Black
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen(
        navController = NavController(LocalContext.current)
    )
}