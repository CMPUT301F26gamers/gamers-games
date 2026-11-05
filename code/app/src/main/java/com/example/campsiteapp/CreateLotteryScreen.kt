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
import com.example.campsiteapp.ui.theme.CampError
import com.example.campsiteapp.ui.theme.CampFieldGrey
import com.example.campsiteapp.ui.theme.CampLabelGrey
import com.example.campsiteapp.ui.theme.CampGreen
import com.example.campsiteapp.ui.theme.CampGreenLight
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

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
        // Header bar, same layout as the profile screen
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
                text = "Create lottery",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

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

/**
 * Small red text shown under a field or form, styled like the profile screen's errors.
 *
 * @param message the text to show
 */
@Composable
private fun FieldError(message: String) {
    Text(
        text = message,
        color = CampError,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

/**
 * Lists the campsites added to the lottery and lets the owner add or remove
 * them (US 02.04.02 for multiple campsites).
 *
 * @param available every campsite the owner can choose from
 * @param selectedIds IDs already added, these are hidden from the dropdown
 * @param onAdd called with the ID of the campsite picked in the dropdown
 * @param onRemove called with the ID of the campsite whose Remove was pressed
 */
@Composable
private fun CampsitePicker(
    available: List<Campsite>,
    selectedIds: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }   // whether the dropdown is showing
    val remaining = available.filter { it.id !in selectedIds }   // not yet added

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Campsites",
            color = CampLabelGrey,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        if (selectedIds.isEmpty()) {
            Text(
                text = "No campsites added yet",
                color = CampLabelGrey,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // One grey row per added campsite
        selectedIds.forEach { id ->
            // Fall back to the raw ID if the campsite isn't in the list
            val name = available.firstOrNull { it.id == id }?.name ?: id
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(CampFieldGrey, RoundedCornerShape(8.dp))
                    .padding(start = 12.dp)
            ) {
                Text(name, color = Color.Black, modifier = Modifier.weight(1f))
                TextButton(onClick = { onRemove(id) }) {
                    Text("Remove", color = CampError)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // The Box anchors the dropdown to the button
        Box {
            // Greyed out once every campsite has been added
            Button(
                onClick = { menuOpen = true },
                enabled = remaining.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CampGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("addCampsiteButton")
            ) {
                Text("Add campsite", fontWeight = FontWeight.Bold)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                remaining.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name) },
                        onClick = { onAdd(option.id); menuOpen = false }
                    )
                }
            }
        }
    }
}

/**
 * A labelled button that opens a date range picker and reports the chosen dates.
 * The button shows the current range, or "Select dates" if none is set.
 *
 * @param label title shown above the button
 * @param earliest dates before this day are not selectable
 * @param start currently chosen start date, or null
 * @param end currently chosen end date, or null
 * @param onConfirm called with the start and end date when OK is pressed
 * @param tag test tag for the button, so each picker can be found in UI tests
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeField(
    label: String,
    earliest: Date,
    start: Date?,
    end: Date?,
    onConfirm: (Date, Date) -> Unit,
    tag: String
) {
    var showPicker by remember { mutableStateOf(false) }   // whether the dialog is open
    val fmt = remember { DateFormat.getDateInstance(DateFormat.MEDIUM) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = CampLabelGrey,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = { showPicker = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = CampFieldGrey,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag(tag)
        ) {
            Text(
                if (start != null && end != null) "${fmt.format(start)} - ${fmt.format(end)}"
                else "Select dates"
            )
        }
    }

    if (showPicker) {
        val local = Calendar.getInstance().apply { time = earliest }
        val minUtcMillis = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis

        val pickerState = rememberDateRangePickerState(
            selectableDates = object : SelectableDates {
                // Grey out every day before the earliest allowed one
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= minUtcMillis
                override fun isSelectableYear(year: Int) = true
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    enabled = pickerState.selectedStartDateMillis != null &&
                            pickerState.selectedEndDateMillis != null,
                    onClick = {
                        onConfirm(
                            Date(pickerState.selectedStartDateMillis!!),
                            Date(pickerState.selectedEndDateMillis!!)
                        )
                        showPicker = false
                    }
                ) {
                    Text("OK", color = CampGreenLight)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel", color = CampGreenLight)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = CampBackground
            ),

            ) {
            MaterialTheme(
                typography = MaterialTheme.typography.copy(
                    bodyLarge = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 12.sp
                    ),
                    titleLarge = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp
                    )
                )
            ) {
                DateRangePicker(
                    state = pickerState,
                    modifier = Modifier.height(500.dp),
                    colors = DatePickerDefaults.colors(
                        dayInSelectionRangeContainerColor = CampGreenLight,
                        containerColor = CampBackground,
                        titleContentColor = Color.White,
                        headlineContentColor = Color.White,
                        weekdayContentColor = CampGreenLight,
                        subheadContentColor = Color.White,
                        yearContentColor = Color.White,
                        currentYearContentColor = CampGreenLight,
                        selectedYearContentColor = Color.White,
                        selectedYearContainerColor = CampGreen,
                        dayContentColor = Color.White,
                        disabledDayContentColor = CampLabelGrey,
                        selectedDayContentColor = Color.White,
                        selectedDayContainerColor = CampGreen,
                        todayContentColor = CampGreenLight,
                        todayDateBorderColor = CampGreenLight,

                        )
                )
            }
        }
    }
}