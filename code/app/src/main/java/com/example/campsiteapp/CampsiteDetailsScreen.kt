package com.example.campsiteapp

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
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
import java.text.DateFormat

@Composable
fun CampsiteDetailsScreen(
    campsite: Campsite,
    profile: Profile,
    lotteries: List<Lottery>,
    joinStateOf: (Lottery) -> JoinState,
    onJoin: (Lottery, (Boolean) -> Unit) -> Unit,
    onLeave: (Lottery, (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    var showUserFields by remember { mutableStateOf(false) }
    if (profile.hasRole(Roles.ENTRANT)) {
        showUserFields = true
    }
    val context = LocalContext.current
    fun say(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CampBackground)
    ) {
        ScreenHeader("Details", onBack)

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
                        containerColor = CampGreen,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .padding(24.dp)
                        .testTag("rateButton")
                ) {
                    Text(text = "Rate")
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            if (showUserFields) {
                Text("Lotteries", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                if (lotteries.isEmpty()) {
                    Text(
                        "This campsite isn't in any lottery right now.",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                lotteries.forEach { lottery ->
                    LotteryRow(
                        lottery = lottery,
                        state = joinStateOf(lottery),
                        canLeave = lottery.canLeave(profile.uid),
                        onJoin = {
                            onJoin(lottery) { ok ->
                                say(
                                    if (ok) "You're on the waitlist for this lottery"
                                    else "Couldn't join the waitlist"
                                )
                            }
                        },
                        onLeave = {
                            onLeave(lottery) { ok ->
                                say(
                                    if (ok) "You left the waitlist"
                                    else "Couldn't leave the waitlist"
                                )
                            }
                        }
                    )
                }
            }
        }

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

@Composable
private fun LotteryRow(
    lottery: Lottery,
    state: JoinState,
    canLeave: Boolean,
    onJoin: () -> Unit,
    onLeave: () -> Unit
) {
    val fmt = remember { DateFormat.getDateInstance(DateFormat.MEDIUM) }
    val waitlistText = lottery.waitlistCapacity
        ?.let { "${lottery.waitlist.size}/$it on waitlist" }
        ?: "${lottery.waitlist.size} on waitlist"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(CampFieldGrey, RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("lotteryRow_${lottery.id}")
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Stay: ${fmt.format(lottery.lotteryStart)} – ${fmt.format(lottery.lotteryEnd)}",
                fontWeight = FontWeight.Bold
            )
            Text(
                "Registration: ${fmt.format(lottery.registrationStart)} – ${fmt.format(lottery.registrationEnd)}",
                fontSize = 13.sp
            )
            Text(waitlistText, fontSize = 13.sp)
        }

        Spacer(Modifier.width(8.dp))

        when (state) {
            JoinState.CAN_JOIN -> Button(
                onClick = onJoin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CampGreenLight, contentColor = Color.Black
                ),
                modifier = Modifier.testTag("joinButton_${lottery.id}")
            ) { Text("Join waitlist") }

            JoinState.ON_WAITLIST -> Button(
                onClick = onLeave,
                enabled = canLeave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red, contentColor = Color.White
                ),
                modifier = Modifier.testTag("leaveButton_${lottery.id}")
            ) { Text("Leave waitlist") }

            JoinState.WAITLIST_FULL -> Button(
                onClick = { }, enabled = false,
                modifier = Modifier.testTag("joinButton_${lottery.id}")
            ) { Text("Waitlist full") }

            JoinState.NOT_OPEN -> Button(
                onClick = { }, enabled = false,
                modifier = Modifier.testTag("joinButton_${lottery.id}")
            ) { Text("Not open") }
        }
    }
}

@Preview
@Composable
fun CampsiteDetailsScreenPreview() {
    CampsiteAppTheme {
        CampsiteDetailsScreen(
            campsite = Campsite(
                "1", "Balls", "Edmonton",
                occupancy = 4,
                description = "A beautiful campsite in the heart of Edmonton.",
                averageRating = 4.0
            ),
            profile = Profile(uid = "1"),
            lotteries = emptyList(),
            joinStateOf = { JoinState.CAN_JOIN },
            onJoin = { _, _ -> },
            onLeave = { _, _ -> },
            onBack = { }
        )
    }
}