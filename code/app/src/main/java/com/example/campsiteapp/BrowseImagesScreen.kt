package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.campsiteapp.ui.theme.CampGreen
import java.text.DateFormat

@Composable
fun BrowseImagesScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    profiles: List<Profile>,
    onDeleteImage: (Profile, String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var selectedProfileId by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showView by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val images = profiles.filter {
        it.profilePicture != null && it.name.contains(search, ignoreCase = true)
    }
    val selectedProfile = images.firstOrNull { it.uid == selectedProfileId }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(title = "All Images", onBack = { navController.popBackStack() })

            TextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search by owner name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag("searchField")
            )

            if (images.isEmpty()) {
                Text(
                    text = "No images uploaded",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(images) { profile ->
                    ImageRow(
                        profile = profile,
                        isSelected = profile.uid == selectedProfileId,
                        onSelect = {
                            selectedProfileId = if (selectedProfileId == it.uid) null else it.uid
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.testTag("viewImageButton"),
                enabled = selectedProfile != null,
                onClick = { showView = true }
            ) {
                Text("View")
            }

            Button(
                modifier = Modifier.testTag("deleteImageButton"),
                enabled = selectedProfile != null,
                onClick = {
                    message = ""
                    showDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text("Delete")
            }
        }
    }

    if (showView && selectedProfile != null) {
        AlertDialog(
            onDismissRequest = { showView = false },
            title = { Text(selectedProfile.name.ifBlank { "Unnamed user" }) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileAvatar(
                        picture = selectedProfile.profilePicture,
                        modifier = Modifier.size(200.dp)
                    )
                    Text("Type: Profile picture")
                    Text(uploadedDate(selectedProfile))
                }
            },
            confirmButton = {
                TextButton(onClick = { showView = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showDialog && selectedProfile != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Remove Image") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Are you sure you want to remove ${selectedProfile.name}'s image?")
                    TextField(
                        value = message,
                        onValueChange = { message = it },
                        placeholder = { Text("Message to owner (optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deleteMessageField")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    modifier = Modifier.testTag("confirmDeleteImage"),
                    onClick = {
                        onDeleteImage(selectedProfile, message.trim())
                        selectedProfileId = null
                        showDialog = false
                    }
                ) {
                    Text("Remove", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    modifier = Modifier.testTag("cancelDeleteImage"),
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ImageRow(
    profile: Profile,
    isSelected: Boolean,
    onSelect: (Profile) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) CampGreen else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .background(Color.LightGray, RoundedCornerShape(8.dp))
            .clickable { onSelect(profile) }
            .padding(12.dp)
            .testTag("imageRow_${profile.uid}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileAvatar(
            picture = profile.profilePicture,
            modifier = Modifier.padding(end = 12.dp).size(56.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = profile.name.ifBlank { "Unnamed user" },
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "Profile picture",
                color = Color.DarkGray
            )

            Text(
                text = uploadedDate(profile),
                color = Color.DarkGray
            )
        }
    }
}

fun uploadedDate(profile: Profile): String {
    val date = profile.profilePictureDate ?: return "Upload date unknown"
    return "Uploaded " + DateFormat.getDateInstance().format(date)
}

@Preview(showBackground = true)
@Composable
fun BrowseImagesPreview() {
    val profiles = listOf(
        Profile(uid = "1", name = "Alex P."),
        Profile(uid = "2", name = "Jane D.")
    )

    BrowseImagesScreen(
        modifier = Modifier.fillMaxSize(),
        navController = rememberNavController(),
        profiles = profiles,
        onDeleteImage = { _, _ -> }
    )
}
