package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun BrowseProfilesScreen (profiles: List<Profile>, onDeleteProfile: (Profile) -> Unit, onViewProfile: (Profile) -> Unit, modifier: Modifier = Modifier) {
    var search by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Text(
            text = "All Profiles",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF006838))
                .padding(16.dp)
        )

        TextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("searchField")
        )

        LazyColumn {
            items(
                profiles.filter {
                    it.name.contains(search, ignoreCase = true)
                }
            ) { profile ->
                ProfileRow(
                    profile = profile,
                    onDeleteProfile = onDeleteProfile,
                    onViewProfile = onViewProfile
                )
            }
        }
    }
}

@Composable
fun ProfileRow(
    profile: Profile,
    onDeleteProfile: (Profile) -> Unit,
    onViewProfile: (Profile) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.LightGray, RoundedCornerShape(8.dp))
            .clickable { onViewProfile(profile) }
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = profile.name,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = Roles.entries
                    .filter { profile.hasRole(it) }
                    .joinToString(", ") { it.name },
                color = Color.DarkGray
            )
        }

        Button(
            modifier = Modifier.testTag("removeProfileButton_${profile.email}"),
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            )
        ) {
            Text("Remove")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Remove Profile") },
            text = {
                Text("Are you sure you want to remove ${profile.name}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProfile(profile)
                        showDialog = false
                    }
                ) {
                    Text("Remove", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BrowseProfilesPreview() {
    val profiles = listOf(
        Profile(uid = "1", name = "Alex P."),
        Profile(uid = "2", name = "Jane D."),
        Profile(uid = "3", name = "Sam K."),
        Profile(uid = "4", name = "Mark T.")
    )

    BrowseProfilesScreen(
        profiles = profiles,
        onDeleteProfile = {},
        onViewProfile = {}
    )
}