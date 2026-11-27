package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.campsiteapp.ui.theme.CampGreen
import com.example.campsiteapp.ui.theme.CampsiteAppTheme

// call with the following
//RatingsScreen(
//campsite = selectedCampsite,
//profile = currentProfile,
//onBack = { navController.popBackStack() },
//onCampsiteUpdated = { updatedCampsite ->
//    repository.updateCampsite(selectedCampsite, updatedCampsite)
//}
//)
@Composable
fun RatingsScreen(
    campsite: Campsite,
    profile: Profile,
    onBack: () -> Unit,
    onCampsiteUpdated: (Campsite) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentCampsite by remember(campsite) { mutableStateOf(campsite) }
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
                text = campsite.name + " Ratings",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(
                items = currentCampsite.ratings,
                key = { _, rating -> rating.userID }
            ) { _, rating ->
                RatingRow(
                    rating = rating
                )
                if (profile.hasRole(Roles.ADMIN)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                    ) {
                        Button(
                            onClick = {
                                val updatedRatings = currentCampsite.ratings.filterNot { it == rating }
                                val newAverage = if (updatedRatings.isEmpty()) {
                                    0.0
                                } else {
                                    updatedRatings.sumOf { it.rating }.toDouble() / updatedRatings.size
                                }
                                val updated = currentCampsite.copy(
                                    ratings = updatedRatings,
                                    averageRating = newAverage
                                )
                                currentCampsite = updated
                                onCampsiteUpdated(updated)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .testTag("remove_rating_${rating.userID}")
                        ) {
                            Text("Remove Rating")
                        }

                        if (rating.review.isNotEmpty()) {
                            Button(
                                onClick = {
                                    val updatedRatings = currentCampsite.ratings.map { item ->
                                        if (item == rating) item.copy(review = "") else item
                                    }
                                    val updated = currentCampsite.copy(
                                        ratings = updatedRatings
                                    )
                                    currentCampsite = updated
                                    onCampsiteUpdated(updated)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .testTag("remove_review_${rating.userID}")
                            ) {
                                Text("Remove Review")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RatingsScreenPreview() {
    val campsite: Campsite = Campsite("1", "Camp Melody")
    campsite.addRating("1", 1)
    campsite.addRating("2", 2, "This Campsite is ass")
    campsite.addRating("3", 5, "The GOAT")
    CampsiteAppTheme {
        RatingsScreen(
            campsite,
            profile = Profile(uid = "1"),
            onBack = { }
        )
    }
}