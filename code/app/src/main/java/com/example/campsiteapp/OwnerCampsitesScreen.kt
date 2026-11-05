package com.example.campsiteapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen displaying a list of campsites owned by the logged-in owner (US 02.03.01).
 *
 * @param campsites The list of campsites to display.
 * @param onBack Callback triggered when the back button is pressed.
 * @param onCampsiteClick Callback triggered when a specific campsite is tapped.
 */
@Composable
        /**
         * Need to have owner id for this to work
         */
fun OwnerCampsitesScreen(
    campsites: List<Campsite>,
    onBack: () -> Unit,
    onCampsiteClick: (Campsite) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back Button
        Button(
            onClick = onBack,
            modifier = Modifier.testTag("backButton")
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "My Campsites",
            fontSize = 26.sp,
            modifier = Modifier.testTag("screenTitle")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // List of Campsites
        if (campsites.isEmpty()) {
            Text("No camp hath yet been writ within thy scroll.", fontSize = 16.sp)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(campsites) { campsite ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCampsiteClick(campsite) }
                            .testTag("campsiteCard")
                    ) {
                        // Just display the campsite name
                        Text(
                            text = campsite.name,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}