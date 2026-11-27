package com.example.campsiteapp

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsiteapp.ui.theme.CampBackground
import com.example.campsiteapp.ui.theme.CampError
import com.example.campsiteapp.ui.theme.CampFieldGrey
import java.text.DateFormat
import java.util.Date

/**
 * Confirmation dialog warning that declining a reservation cannot be undone
 */
@Composable
fun DeclineDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Decline reservation?") },
        text = { Text("This cannot be undone. The lottery will pick a new winner.") },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.testTag("confirmDecline")) {
                Text("Decline", color = CampError)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancelDecline")) { Text("Cancel") }
        }
    )
}

/**
 * Screen where an entrant sees the campsites they were drawn for and still have to
 * reserve or decline
 *
 * Each row is one campsite from a lottery the user won. Tapping a row opens
 * [ReservationScreen]. When the list is empty a message is shown.
 * All data comes from the repositories
 */
@Composable
fun PendingReservationsScreen(
    userId: String,
    lotteryRepo: LotteryRepository,
    campsiteRepo: CampsiteRepository,
    onPick: (lotteryId: String, campsiteId: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val won = lotteryRepo.pendingLotteries(userId)
    var declining by remember { mutableStateOf<Lottery?>(null) }
    val fmt = remember { DateFormat.getDateInstance(DateFormat.MEDIUM) }

    Column(Modifier.fillMaxSize().background(CampBackground)) {
        ScreenHeader(title = "Pending Reservations", onBack = onBack)

        // Greyed out when there is nothing to interact with
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("pendingSection")
        ) {
            if (won.isEmpty()) {
                Text("You have no pending reservations", color = Color.White, modifier = Modifier.testTag("emptyPending"))
            }
            won.forEach { lottery ->
                lottery.campsiteIds.forEach { campsiteId ->
                    val name = campsiteRepo.get(campsiteId)?.name ?: campsiteId
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .background(CampFieldGrey, RoundedCornerShape(12.dp))
                            .clickable { onPick(lottery.id, campsiteId) }
                            .padding(12.dp)
                            .testTag("pendingRow_$campsiteId")
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                            Text(
                                "Stay: ${fmt.format(lottery.lotteryStart)} – ${fmt.format(lottery.lotteryEnd)}",
                                color = Color.DarkGray, fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    declining?.let { lottery ->
        DeclineDialog(
            onConfirm = {
                declining = null
                lotteryRepo.decline(lottery.id, userId, Date()) { ok ->
                    if (!ok) Toast.makeText(context, "Couldn't decline. Is booking open?", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { declining = null }
        )
    }
}