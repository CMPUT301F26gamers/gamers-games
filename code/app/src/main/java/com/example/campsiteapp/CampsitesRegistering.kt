package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsiteapp.ui.theme.CampBackground
import com.example.campsiteapp.ui.theme.CampError
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
    onRegister: (name: String, location: String, maxGuests: Int, description: String, tags: List<String>) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var maxGuests by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }
    var locationError by remember { mutableStateOf("") }
    var maxGuestsError by remember { mutableStateOf("") }
    val validator = remember { CampsiteValidator() }

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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Campsite Name") },
                modifier = Modifier.fillMaxWidth().testTag("nameInput")
            )
            if (nameError.isNotEmpty()) {
                Text(
                    text = nameError,
                    color = CampError,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth().testTag("locationInput")
            )
            if (locationError.isNotEmpty()) {
                Text(
                    text = locationError,
                    color = CampError,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = maxGuests,
                onValueChange = { maxGuests = it },
                label = { Text("Max guests") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("maxGuestsInput")
            )
            if (maxGuestsError.isNotEmpty()) {
                Text(
                    text = maxGuestsError,
                    color = CampError,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (amenities)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth().testTag("descriptionInput")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (separate with commas)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("tagsInput")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    nameError = if (validator.isValidName(name)) "" else "Name can't be empty"
                    locationError = if (validator.isValidLocation(location)) "" else "Location can't be empty"
                    maxGuestsError = if (validator.isValidMaxGuests(maxGuests)) "" else "Max guests must be a whole number of 1 or more"

                    if (nameError.isEmpty() && locationError.isEmpty() && maxGuestsError.isEmpty()) {
                        onRegister(
                            name.trim(),
                            location.trim(),
                            maxGuests.trim().toInt(),
                            description.trim(),
                            validator.parseTags(tags)
                        )
                    }
                },
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
            onRegister = { _, _, _, _, _ -> },
            onBack = {}
        )
    }
}