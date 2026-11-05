package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsiteapp.ui.theme.CampAvatarGrey
import com.example.campsiteapp.ui.theme.CampBackground
import com.example.campsiteapp.ui.theme.CampError
import com.example.campsiteapp.ui.theme.CampLabelGrey
import com.example.campsiteapp.ui.theme.CampFieldGrey
import com.example.campsiteapp.ui.theme.CampGreen
import com.example.campsiteapp.ui.theme.CampGreenLight
import com.example.campsiteapp.ui.theme.CampRed
import com.example.campsiteapp.ui.theme.CampsiteAppTheme

@Composable
fun ProfileScreen(
    name: String,
    email: String,
    phone: String,
    onSave: (String, String, String) -> Unit,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val validator = ProfileValidator()

    var nameInput by remember { mutableStateOf(name) }
    var emailInput by remember { mutableStateOf(email) }
    var phoneInput by remember { mutableStateOf(phone) }
    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CampBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CampGreen)
                .statusBarsPadding()
                .height(56.dp)
        ) {
            TextButton(
                onClick = { onBack() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .testTag("backButton")
            ) {
                Text(text = "←", color = Color.White, fontSize = 22.sp)
            }

            Text(
                text = "Profile",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(CampAvatarGrey, CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = nameInput,
                onValueChange = {
                    nameInput = it
                    saved = false
                },
                label = { Text("Name") },
                colors = fieldColors(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("nameInput")
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

            TextField(
                value = emailInput,
                onValueChange = {
                    emailInput = it
                    saved = false
                },
                label = { Text("Email") },
                colors = fieldColors(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("emailInput")
            )
            if (emailError.isNotEmpty()) {
                Text(
                    text = emailError,
                    color = CampError,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = phoneInput,
                onValueChange = {
                    phoneInput = it
                    saved = false
                },
                label = { Text("Phone (optional)") },
                colors = fieldColors(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("phoneInput")
            )
            if (phoneError.isNotEmpty()) {
                Text(
                    text = phoneError,
                    color = CampError,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    nameError = ""
                    emailError = ""
                    phoneError = ""

                    if (!validator.isValidName(nameInput)) {
                        nameError = "Name can't be empty"
                    }
                    if (!validator.isValidEmail(emailInput)) {
                        emailError = "Enter a valid email"
                    }
                    if (!validator.isValidPhone(phoneInput)) {
                        phoneError = "Enter a valid phone number"
                    }

                    if (nameError.isEmpty() && emailError.isEmpty() && phoneError.isEmpty()) {
                        onSave(nameInput.trim(), emailInput.trim(), phoneInput.trim())
                        saved = true
                    } else {
                        saved = false
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CampGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("saveButton")
            ) {
                Text(text = "Save", fontWeight = FontWeight.Bold)
            }

            if (saved) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Profile saved",
                    color = CampGreenLight,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CampRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("deleteProfileButton")
            ) {
                Text(text = "Delete profile", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Profile") },
            text = {
                Text("Are you sure you want to delete your profile? This is permanent and cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    modifier = Modifier.testTag("confirmDelete"),
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = CampRed)
                }
            },
            dismissButton = {
                TextButton(
                    modifier = Modifier.testTag("cancelDelete"),
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun fieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = CampFieldGrey,
    unfocusedContainerColor = CampFieldGrey,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    focusedLabelColor = CampGreen,
    unfocusedLabelColor = CampLabelGrey,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    cursorColor = CampGreen
)

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    CampsiteAppTheme {
        ProfileScreen(
            name = "Alex P.",
            email = "alex@example.com",
            phone = "780-555-0123",
            onSave = { _, _, _ -> },
            onBack = {},
            onDelete = {}
        )
    }
}
