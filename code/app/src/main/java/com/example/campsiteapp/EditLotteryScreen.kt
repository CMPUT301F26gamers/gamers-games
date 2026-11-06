package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.campsiteapp.ui.theme.CampGreen
import com.example.campsiteapp.ui.theme.CampLabelGrey
import java.util.Date

@Composable
fun EditLotteryScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    campsites: List<Campsite>,
    vm: EditLotteryViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(vm.isSaved()) {
        if (vm.isSaved()) {
            vm.onSavedHandled()
            onSaved()
        }
    }

    val winnersInvalid = vm.getWinnersText().toIntOrNull()?.let { it < 1 } ?: true
    val capacityInvalid = vm.getCapacityText().isNotBlank() &&
            (vm.getCapacityText().toIntOrNull()?.let { it < 1 } ?: true)

    Column(modifier = modifier.fillMaxSize().background(CampBackground)) {

        ScreenHeader(title = "Edit Lottery", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            CampsitePicker(
                available = campsites,
                selectedIds = vm.getSelectedCampsiteIds(),
                onAdd = { vm.addCampsite(it) },
                onRemove = { vm.removeCampsite(it) }
            )

            Spacer(Modifier.height(16.dp))

            TextField(
                value = vm.getWinnersText(),
                onValueChange = { vm.onWinnersChange(it) },
                label = { Text("Number of winners") },
                colors = fieldColors(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("winnersInput")
            )
            if (winnersInvalid) FieldError("Enter a whole number of 1 or more")

            Spacer(Modifier.height(8.dp))

            TextField(
                value = vm.getCapacityText(),
                onValueChange = { vm.onCapacityChange(it) },
                label = { Text("Waitlist capacity (optional)") },
                colors = fieldColors(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("capacityInput")
            )
            if (capacityInvalid) {
                FieldError("Enter a whole number of 1 or more, or leave empty")
            } else {
                Text(
                    "Leave empty for no limit",
                    color = CampLabelGrey,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            DateRangeField(
                label = "Registration period",
                earliest = Date(),
                start = vm.getRegistrationStart(),
                end = vm.getRegistrationEnd(),
                onConfirm = { s, e -> vm.setRegistrationPeriod(s, e) },
                tag = "registrationButton"
            )
            Spacer(Modifier.height(8.dp))
            DateRangeField(
                label = "Booking period",
                earliest = vm.getRegistrationEnd()?.let { Date(it.time + 1) } ?: Date(),
                start = vm.getBookingStart(),
                end = vm.getBookingEnd(),
                onConfirm = { s, e -> vm.setBookingPeriod(s, e) },
                tag = "bookingButton"
            )
            Spacer(Modifier.height(8.dp))
            DateRangeField(
                label = "Stay period",
                earliest = vm.getBookingEnd()?.let { Date(it.time + 1) } ?: Date(),
                start = vm.getStayStart(),
                end = vm.getStayEnd(),
                onConfirm = { s, e -> vm.setStayPeriod(s, e) },
                tag = "stayButton"
            )

            vm.getError()?.let {
                Spacer(Modifier.height(12.dp))
                FieldError(it)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { vm.save() },
                enabled = !vm.isSaving(),
                colors = ButtonDefaults.buttonColors(containerColor = CampGreen, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("saveButton")
            ) {
                Text(if (vm.isSaving()) "Saving..." else "Save changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}