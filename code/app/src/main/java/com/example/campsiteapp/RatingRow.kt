package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsiteapp.ui.theme.CampFieldGrey
import com.example.campsiteapp.ui.theme.CampGreen

@Composable
fun RatingRow(
    rating: Rating,
//    isAdmin: Boolean,
//    onRemoveRating: (Rating) -> Unit,
//    onRemoveReview: (Rating) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = CampFieldGrey,
                    shape = RoundedCornerShape(16.dp)
                )
                .statusBarsPadding()
                .height(110.dp)
                .padding(horizontal = 16.dp)
        ) {
            Column() {
//                if (isAdmin) {
//                    Text(
//                        text = rating.userID,
//                        fontSize = 30.sp,
//                        modifier = Modifier
//                    )
//                }
                Row() {
                    Text(
                        text = "Rating: " + rating.rating,
                        fontSize = 30.sp,
                        modifier = Modifier
                    )
//                    if (isAdmin) {
//                        Button(
//                            onClick = { onRemoveRating(rating) },
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = Color.Red,
//                                contentColor = Color.White
//                            ),
//                            modifier = Modifier
//                        ) {
//                            Text("Remove Rating")
//                        }
//                    }
                }
                if (rating.review != "") {
                    Text(
                        text = rating.review,
                        fontSize = 20.sp,
                        modifier = Modifier
                    )
//                    if (isAdmin) {
//                        Button(
//                            onClick = { onRemoveReview(rating) },
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = Color.Red,
//                                contentColor = Color.White
//                            ),
//                            modifier = Modifier
//                        ) {
//                            Text("Remove Review")
//                        }
//                    }
                }
            }
        }
    }
}