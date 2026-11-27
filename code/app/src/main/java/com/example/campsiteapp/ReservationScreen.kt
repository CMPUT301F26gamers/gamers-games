package com.example.campsiteapp

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campsiteapp.ui.theme.CampBackground
import com.example.campsiteapp.ui.theme.CampError
import com.example.campsiteapp.ui.theme.CampGreen
import com.example.campsiteapp.ui.theme.CampGreenLight
import com.example.campsiteapp.ui.theme.CampLabelGrey
import androidx.compose.material3.SelectableDates
import com.example.campsiteapp.ui.theme.CampRed
import java.util.Date

/**
 * Screen where an entrant picks dates for one campsite they won and confirms or
 * declines
 *
 * The calendar only allows days that are inside the stay period and not already
 * reserved, and it refreshes when availability changes. Confirm is enabled once a
 * valid range is selected and booking is open. Logic using
 * [checkReservation]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(
    lottery: Lottery,
    campsite: Campsite,
    userId: String,
    reservationRepo: ReservationRepository,
    lotteryRepo: LotteryRepository,
    onFinished: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    // Reads the repo's state list, so this recomputes when someone else reserves (live refresh)
    val available = reservationRepo.availableDays(lottery, campsite.id)
    var error by remember { mutableStateOf<String?>(null) }
    var showDecline by remember { mutableStateOf(false) }
    val bookingOpen = lottery.isBookingOpen(Date())

    Column(Modifier.fillMaxSize().background(CampBackground)) {
        ScreenHeader(title = campsite.name, onBack = onBack)

        Column(Modifier.weight(1f)) {
            if (available.isEmpty()) {
                Text(
                    "No dates are available for this campsite",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp).testTag("noDates")
                )
            } else {
                key(available) {   // rebuild the picker when availability changes
                    val availableSet = available.toSet()
                    val state = rememberDateRangePickerState(
                        initialDisplayedMonthMillis = dayToPickerMillis(available.first()),
                        selectableDates = object : SelectableDates {
                            override fun isSelectableDate(utcTimeMillis: Long) =
                                pickerMillisToDay(utcTimeMillis) in availableSet
                            override fun isSelectableYear(year: Int) = true
                        }
                    )
                    val first = state.selectedStartDateMillis?.let { pickerMillisToDay(it) }
                    val last = state.selectedEndDateMillis?.let { pickerMillisToDay(it) } ?: first
                    val valid = first != null && last != null && (first..last).all { it in availableSet }

                    DateRangePicker(
                        state = state,
                        modifier = Modifier.weight(1f),
                        colors = DatePickerDefaults.colors(
                            containerColor = CampBackground,
                            titleContentColor = Color.White,
                            headlineContentColor = Color.White,
                            weekdayContentColor = CampGreenLight,
                            subheadContentColor = Color.White,
                            dayContentColor = Color.White,
                            disabledDayContentColor = CampLabelGrey,
                            selectedDayContentColor = Color.White,
                            selectedDayContainerColor = CampGreen,
                            dayInSelectionRangeContainerColor = CampGreenLight,
                            todayContentColor = CampGreenLight,
                            todayDateBorderColor = CampGreenLight
                        )
                    )

                    if (!bookingOpen) {
                        Text("Booking is not open right now", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    error?.let { FieldError(it) }

                    Button(
                        enabled = valid && bookingOpen,
                        onClick = {
                            if (first != null && last != null) {
                                reservationRepo.reserve(lottery, campsite.id, userId, first, last) { result ->
                                    when (result) {
                                        ReserveResult.SAVED -> onFinished()
                                        ReserveResult.TAKEN -> error = "Those dates were just taken. Please pick again."
                                        ReserveResult.BOOKING_CLOSED -> error = "Booking is closed."
                                        else -> error = "Couldn't save the reservation. Try again."
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CampGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp).testTag("confirmButton")
                    ) { Text("Confirm", fontWeight = FontWeight.Bold) }
                }
            }
        }

        Button(
            onClick = { showDecline = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = CampRed,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()                      // lifts it above the system nav bar
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(48.dp)
                .testTag("declineButton")
        ) {
            Text("Decline reservation", fontWeight = FontWeight.Bold)
        }
    }

    if (showDecline) {
        DeclineDialog(
            onConfirm = {
                showDecline = false
                lotteryRepo.decline(lottery.id, userId, Date()) { ok ->
                    if (ok) onFinished()
                    else Toast.makeText(context, "Couldn't decline. Is booking open?", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showDecline = false }
        )
    }
}