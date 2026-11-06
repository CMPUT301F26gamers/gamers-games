package com.example.campsiteapp

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsiteapp.ui.theme.CampBackground
import com.example.campsiteapp.ui.theme.CampFieldGrey
import com.example.campsiteapp.ui.theme.CampGreen
import com.example.campsiteapp.ui.theme.CampLabelGrey

@Composable
fun CampsiteRow(
    campsite: Campsite,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                Text(
                    text = campsite.name,
                    fontSize = 30.sp,
                    modifier = Modifier
                )
                Text(
                    text = "Location: " + campsite.location,
                    fontSize = 15.sp,
                    modifier = Modifier
                )
                Text(
                    text = "Occupancy: " + campsite.occupancy,
                    fontSize = 15.sp,
                    modifier = Modifier
                )
                Text(
                    text = "Rating: " + campsite.averageRating + "/5",
                    fontSize = 15.sp,
                    modifier = Modifier
                )
            }
        }
    }
}