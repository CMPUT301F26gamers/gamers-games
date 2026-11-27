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

@Composable
fun RatingsScreen(
    campsite: Campsite,
    profile: Profile,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            itemsIndexed(campsite.ratings) { index, rating ->
                RatingRow(
                    rating = rating
                )
                if (profile.hasRole(Roles.ADMIN)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                    ) {
                        Button(
                            onClick = { campsite.removeRating(rating) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .testTag("remove_rating_${rating.userID}")
                        ) {
                            Text("Remove Rating")
                        }
                        if (rating.review != "") {
                            Button(
                                onClick = { campsite.removeReview(rating) },
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