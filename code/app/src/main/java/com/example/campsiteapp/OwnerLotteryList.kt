package com.example.campsiteapp

import androidx.compose.foundation.background
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

/**
 * Make this for the test
 */

data class WaitlistEntry(
    val name: String,
    val joinDate: String, // e.g., "2026-11-04"
    val status: String = "Pending" // Can be "Pending", "Won", or "Not selected"
)

/**
 * Owner screen displaying a list of entrants who have joined the waitlist for a campsite lottery (US 02.09.01).
 *
 * @param entrants The list of entrants on the waitlist.
 * @param totalEntrants The total number of entrants.
 * @param capacity The max capacity of the waitlist (US 02.04.07).
 * @param isDrawComplete Whether the lottery has been drawn (US 02.05.01).
 * @param onBack Callback triggered when the back button is pressed.
 */

@Composable
fun OwnerLotteryListScreen(
    entrants: List<WaitlistEntry>,
    capacity: Int?,
    isDrawComplete: Boolean,
    onBack: () -> Unit
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
                .statusBarsPadding()
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
                text = "Lottery Waitlist",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag("screenTitle")
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val capacityText = if (capacity != null) " / $capacity" else ""
            Text(
                text = "Total Entrants: ${entrants.size}$capacityText",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = CampGreen,
                modifier = Modifier.testTag("totalEntrantsText")
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (entrants.isEmpty()) {
                Text(
                    text = "No one has joined the waitlist yet.",
                    fontSize = 16.sp,
                    modifier = Modifier.testTag("emptyMessage")
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entrants) { entrant ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("entrantCard"),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = entrant.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                                    if (isDrawComplete) {
                                        Text(
                                            text = entrant.status,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (entrant.status == "Won") CampGreen else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Joined: ${entrant.joinDate}", fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}


