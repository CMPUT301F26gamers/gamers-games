package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsiteapp.ui.theme.CampBackground
import com.example.campsiteapp.ui.theme.CampGreen
import androidx.compose.ui.tooling.preview.Preview
import com.example.campsiteapp.ui.theme.CampsiteAppTheme

/**
 * Screen for a campsite owner to register a new campsite (US 02.01.01).
 * Accepts basic details and triggers the save callback.
 *
 * @param onRegister Callback triggered with the new campsite details.
 * @param onBack Callback to return to the previous screen.
 */
@Composable
fun RegisterCampsiteScreen(
    onRegister: (name: String, location: String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CampBackground)
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CampGreen)
                .height(56.dp)
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .testTag("backButton")
            ) {
                Text(text = "←", color = Color.White, fontSize = 22.sp)
            }

            Text(
                text = "Register Campsite",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag("screenTitle")
            )
        }

        // Form Fields
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Campsite Name") },
                modifier = Modifier.fillMaxWidth().testTag("nameInput")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth().testTag("locationInput")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onRegister(name, location) },
                colors = ButtonDefaults.buttonColors(containerColor = CampGreen),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("registerButton")
            ) {
                Text("Register", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true, name = "Register Campsite Preview")
@Composable
fun RegisterCampsiteScreenPreview() {
    CampsiteAppTheme {
        RegisterCampsiteScreen(
            onRegister = { _, _ -> },
            onBack = {}
        )
    }
}