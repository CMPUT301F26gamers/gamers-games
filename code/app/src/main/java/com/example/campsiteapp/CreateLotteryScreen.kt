package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsiteapp.ui.theme.CampBackground
import com.example.campsiteapp.ui.theme.CampLabelGrey
import com.example.campsiteapp.ui.theme.CampGreen
import java.util.Date
/**
 * Screen where a campsite owner creates a lottery.
 *
 * The owner picks campsites, the number of winners, an optional waitlist
 * capacity and the three periods, then presses create. All state lives in
 * [vm], this composable only displays it and forwards events.
 *
 * @param onBack called when the back arrow is pressed
 * @param onCreated called once after a lottery was saved
 * @param campsites every campsite the owner can choose from
 * @param vm holds the form state and does the saving
 * @param modifier modifier for the root column
 */
@Composable
fun CreateLotteryScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    ownerId: String,
    campsites: List<Campsite>,
    vm: CreateLotteryViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(vm.isCreated()) {
        if (vm.isCreated()) {
            vm.onCreatedHandled()
            onCreated()
        }
    }

    // what to show as red hints under the fields
    val winnersInvalid = vm.getWinnersText().toIntOrNull()?.let { it < 1 } ?: true
    val capacityInvalid = vm.getCapacityText().isNotBlank() &&
            (vm.getCapacityText().toIntOrNull()?.let { it < 1 } ?: true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CampBackground)
    ) {
        ScreenHeader(title = "Create Lottery", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            CampsitePicker(
                available = campsites,
                selectedIds = vm.getSelectedCampsiteIds(),
                onAdd = { id -> vm.addCampsite(id) },
                onRemove = { id -> vm.removeCampsite(id) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Number of winners (US 02.04.05)
            TextField(
                value = vm.getWinnersText(),
                onValueChange = { text -> vm.onWinnersChange(text) },
                label = { Text("Number of winners") },
                colors = fieldColors(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("winnersInput")
            )
            if (winnersInvalid) {
                FieldError("Enter a whole number of 1 or more")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Waitlist capacity, optional (US 02.04.07)
            TextField(
                value = vm.getCapacityText(),
                onValueChange = { text -> vm.onCapacityChange(text) },
                label = { Text("Waitlist capacity (optional)") },
                colors = fieldColors(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("capacityInput")
            )
            if (capacityInvalid) {
                FieldError("Enter a whole number of 1 or more, or leave empty")
            } else {
                // Neutral hint when the field is empty or valid
                Text(
                    text = "Leave empty for no limit",
                    color = CampLabelGrey,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Registration: users join the waitlist (US 02.04.03)
            DateRangeField(
                label = "Registration period",
                earliest = Date(),
                start = vm.getRegistrationStart(),
                end = vm.getRegistrationEnd(),
                onConfirm = { start, end -> vm.setRegistrationPeriod(start, end) },
                tag = "registrationButton"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Booking: drawn winners book or decline
            DateRangeField(
                label = "Booking period",
                earliest = vm.getRegistrationEnd()?.let { Date(it.time + 1) } ?: Date(),
                start = vm.getBookingStart(),
                end = vm.getBookingEnd(),
                onConfirm = { start, end -> vm.setBookingPeriod(start, end) },
                tag = "bookingButton"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Stay: when people actually camp (US 02.04.01)
            DateRangeField(
                label = "Stay period",
                earliest = vm.getBookingEnd()?.let { Date(it.time + 1) } ?: Date(),
                start = vm.getStayStart(),
                end = vm.getStayEnd(),
                onConfirm = { start, end -> vm.setStayPeriod(start, end) },
                tag = "stayButton"
            )

            // Validation or save error from the ViewModel
            vm.getError()?.let {
                Spacer(modifier = Modifier.height(12.dp))
                FieldError(it)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // disable while saving to avoid duplicates
            Button(
                onClick = { vm.createLottery(ownerId) },
                enabled = !vm.isSaving(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CampGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("createButton")
            ) {
                Text(
                    text = if (vm.isSaving()) "Creating..." else "Create lottery",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}



