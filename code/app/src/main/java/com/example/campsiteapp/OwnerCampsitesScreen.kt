package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsiteapp.ui.theme.CampBackground
import com.example.campsiteapp.ui.theme.CampGreen
import com.example.campsiteapp.ui.theme.CampFieldGrey

/**
 * Screen displaying a list of campsites owned by the logged-in owner (US 02.03.01).
 *
 * @param campsites The list of campsites to display.
 * @param onBack Callback triggered when the back button is pressed.
 * @param onCampsiteClick Callback triggered when a specific campsite is tapped.
 */
@Composable
fun OwnerCampsitesScreen(
    campsites: List<Campsite>,
    onBack: () -> Unit,
    onCampsiteClick: (Campsite) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CampBackground)
    ) {
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
                text = "My Campsites",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag("screenTitle")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List of Campsites
        if (campsites.isEmpty()) {
            Text(
                text = "No camp hath yet been writ within thy scroll.",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(campsites) { campsite ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCampsiteClick(campsite) }
                            .testTag("campsiteCard"),
                        colors = CardDefaults.cardColors(containerColor = CampFieldGrey)
                    ) {
                        // Just display the campsite name
                        Text(
                            text = campsite.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}