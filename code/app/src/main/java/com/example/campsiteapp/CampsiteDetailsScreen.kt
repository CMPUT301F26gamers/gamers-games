package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import com.example.campsiteapp.ui.theme.CampBackground
import com.example.campsiteapp.ui.theme.CampFieldGrey
import com.example.campsiteapp.ui.theme.CampGreen
import com.example.campsiteapp.ui.theme.CampGreenLight
import com.example.campsiteapp.ui.theme.CampsiteAppTheme

@Composable
fun CampsiteDetailsScreen(
    campsite: Campsite,
    profile: Profile,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    var showUserFields by remember { mutableStateOf(false) }
    var showRateFields by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf("") }
    if (profile.hasRole(Roles.ENTRANT)) {
        showUserFields = true
    }

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
                onClick = { },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .testTag("backButton")
            ) {
                Text(text = "←", color = Color.White, fontSize = 22.sp)
            }
            Text(
                text = "Details",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = CampFieldGrey,
                    shape = RoundedCornerShape(16.dp)
                )
                .statusBarsPadding()
                .height(300.dp)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                Text(
                    text = "Campsite ID: " + campsite.id,
                    fontSize = 30.sp,
                    modifier = Modifier
                )
                Text(
                    text = "Name: " + campsite.name,
                    fontSize = 30.sp,
                    modifier = Modifier
                )
                Text(
                    text = "Location: " + campsite.location,
                    fontSize = 30.sp,
                    modifier = Modifier
                )
                Text(
                    text = "Maximum Occupancy: " + campsite.occupancy,
                    fontSize = 30.sp,
                    modifier = Modifier
                )
                Text(
                    text = "Rating: " + campsite.averageRating + "/5",
                    fontSize = 30.sp,
                    modifier = Modifier
                )
                Text(
                    text = campsite.description,
                    fontSize = 30.sp,
                    modifier = Modifier.testTag("descriptionText")
                )
                if (campsite.tags.isNotEmpty()) {
                    Text(
                        text = "Tags: " + campsite.tags.joinToString(", "),
                        fontSize = 30.sp,
                        modifier = Modifier.testTag("tagsText")
                    )
                }
            }
        }
        if (showUserFields) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CampGreenLight,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .padding(24.dp)
                        .testTag("lotteryButton")
                ) {
                    Text(
                        text = "Enter Lottery"
                    )
                }
                Button(
                    onClick = { showRateFields = !showRateFields },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CampGreen,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .padding(24.dp)
                        .testTag("rateButton")
                ) {
                    Text(
                        text = "Rate"
                    )
                }
            }
        }

        if (showRateFields) {
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                TextField(
                    value = rating,
                    onValueChange = { rating = it },
                    label = { Text("Rating (1-5)") },
                    colors = fieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rating")
                )
                Button(
                    onClick = {
                        if (rating.isNotBlank()) {
                            campsite.addRating(profile.uid, rating = rating.toInt())
                            showRateFields = !showRateFields
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CampGreenLight,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .padding(24.dp)
                        .testTag("leaveRatingButton")
                ) {
                    Text(
                        text = "Leave Rating"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CampGreen)
                .statusBarsPadding()
                .height(56.dp)
        ) {

        }
    }


}

@Preview
@Composable
fun CampsiteDetailsScreenPreview() {
    CampsiteAppTheme {
        CampsiteDetailsScreen(
            campsite = Campsite(
                "1",
                "Balls",
                "Edmonton",
                occupancy = 4,
                description = "A beautiful campsite in the heart of Edmonton.",
                averageRating = 0.0
            ),
            profile = Profile(
                uid = "1"
            ),
            onBack = { }
        )
    }
}