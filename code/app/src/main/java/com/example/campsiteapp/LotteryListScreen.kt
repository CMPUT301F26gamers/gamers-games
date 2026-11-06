package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsiteapp.ui.theme.CampBackground
import com.example.campsiteapp.ui.theme.CampError
import com.example.campsiteapp.ui.theme.CampFieldGrey
import com.example.campsiteapp.ui.theme.CampGreen
import com.example.campsiteapp.ui.theme.CampLabelGrey
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LotteryListScreen(
    profile: Profile,
    lotteryRepo: LotteryRepository,
    campsiteRepo: CampsiteRepository,
    onEdit: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val myLotteries = lotteryRepo.getLotteriesByOwner(profile.uid)
    val dateFmt = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Column(modifier = modifier.fillMaxSize().background(CampBackground)) {
        ScreenHeader(title = "My Lotteries", onBack = onBack)

        if (myLotteries.isEmpty()) {
            Text(
                "You haven't created any lotteries yet",
                color = CampLabelGrey,
                modifier = Modifier.padding(16.dp).testTag("emptyLotteries")
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.testTag("ownerLotteryList")
        ) {
            items(myLotteries, key = { it.id }) { lottery ->
                val names = lottery.campsiteIds.joinToString { id ->
                    campsiteRepo.campsites.firstOrNull { it.id == id }?.name ?: id
                }
                val capacity = lottery.waitlistCapacity?.toString() ?: "no limit"

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CampFieldGrey, RoundedCornerShape(12.dp))
                        .clickable { onEdit(lottery.id) }
                        .padding(12.dp)
                        .testTag("lotteryCard")
                ) {
                    Text(names, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "Registration: ${dateFmt.format(lottery.registrationStart)} – " +
                                dateFmt.format(lottery.registrationEnd)
                    )
                    Text(
                        "Stay: ${dateFmt.format(lottery.lotteryStart)} – " +
                                dateFmt.format(lottery.lotteryEnd)
                    )
                    Text("Winners: ${lottery.numWinners}")
                    Text("Waitlist: ${lottery.waitlist.size} / $capacity")

                    TextButton(
                        onClick = { lotteryRepo.removeLottery(lottery.id) },
                        modifier = Modifier.align(Alignment.End).testTag("deleteLottery")
                    ) { Text("Delete", color = CampError) }
                }
            }
        }
    }
}