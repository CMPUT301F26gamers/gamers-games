package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.campsiteapp.ui.theme.CampGreenLight
import com.example.campsiteapp.ui.theme.CampLabelGrey
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Small red text shown under a field or form, styled like the profile screen's errors.
 *
 * @param message the text to show
 */
@Composable
fun FieldError(message: String) {
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
fun CampsitePicker(
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
fun DateRangeField(
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

/**
 * Green top bar with a back arrow and a centered title.
 *
 * @param title text shown in the middle
 * @param onBack called when the back arrow is pressed
 */
@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
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
            Text("←", color = Color.White, fontSize = 22.sp)
        }
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}